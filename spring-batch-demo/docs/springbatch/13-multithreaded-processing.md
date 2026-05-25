# Chapter 13: Multi-threaded Processing — 底层原理与实战

## Overview

掌握 Spring Batch 多线程处理的四种并行策略：多线程 Step、异步 Processor/Writer、分区（Partitioning）和远程分块（Remote Chunking）。本章深入每种策略的底层架构、线程模型、事务边界和源码实现原理。

```
并行处理策略全景
├── 1. 多线程 Step (Multi-threaded Step)
│   └── 同一 Step 内多个 chunk 并行执行
├── 2. 异步 Processor/Writer (AsyncItemProcessor/AsyncItemWriter)
│   └── Processor 阶段多线程并发，Reader/Writer 单线程
├── 3. 分区 (Partitioning)
│   └── 数据分片，每个分片独立 Step 执行
└── 4. 远程分块 (Remote Chunking)
    └── 跨 JVM 分布式执行
```

---

## 13.1 线程模型全景对比

### 13.1.1 四种策略核心差异

| 特性 | 多线程 Step | AsyncItemProcessor/Writer | 分区 | 远程分块 |
|------|-------------|--------------------------|------|---------|
| **并行粒度** | Chunk 级别 | Item 级别 | Step 级别 | Chunk 级别 |
| **Reader 线程安全** | 必须 | 不需要（单线程读） | 不需要（每分区独立） | 不需要（Master 单线程读） |
| **事务边界** | 每 Chunk 独立事务 | 单线程事务 | 每分区独立事务 | 每 Chunk 独立事务 |
| **网络开销** | 无 | 无 | 无 | 有 |
| **复杂度** | 低 | 中 | 中 | 高 |
| **适用场景** | I/O 密集型读+写 | Processor 慢（API/计算） | 数据可水平切分 | 跨机器分布 |

### 13.1.2 执行模型对比图

```
1) 单线程 Step（基准）:
Thread-1: [Read→Process→Write] → [Read→Process→Write] → [Read→Process→Write]
           chunk-1                     chunk-2                    chunk-3
           ├──── tx1 ────┤             ├──── tx2 ────┤           ├──── tx3 ────┤
           time →

2) 多线程 Step:
Thread-1: [Read→Process→Write] → [Read→Process→Write]
           chunk-1                    chunk-3
           ├──── tx1 ────┤           ├──── tx3 ────┤
Thread-2: [Read→Process→Write] → [Read→Process→Write]
           chunk-2                    chunk-4
           ├──── tx2 ────┤           ├──── tx4 ────┤
           time →

3) AsyncItemProcessor/AsyncItemWriter:
Thread-1 (Reader): [Read item1] [Read item2] [Read item3] [Read item4] ...
                     ↓ Future  ↓ Future  ↓ Future  ↓ Future
Thread-2 (Process):  [Process item1] ...
Thread-3 (Process):                [Process item2] ...
Thread-4 (Process):                              [Process item3] ...
Thread-1 (Writer):   [等待所有Future.get() → 批量写入]
                     ├───────── chunk-1 ─────────┤

4) 分区:
Master Thread: [Split data → Assign partitions]
                │              │              │
Thread-1:    [StepExec-1: R→P→W]
                ├── tx1 ──┤
Thread-2:                  [StepExec-2: R→P→W]
                              ├── tx2 ──┤
Thread-3:                                [StepExec-3: R→P→W]
                                            ├── tx3 ──┤
```

---

## 13.2 多线程 Step（Multi-threaded Step）底层原理

### 13.2.1 概念

多线程 Step 是 Spring Batch 中最直接的并行方式：在 Chunk-oriented Step 上配置一个 `TaskExecutor`，每个 Chunk 的**读取→处理→写入**循环在不同的线程中执行。

### 13.2.2 配置示例

```java
@Configuration
public class MultiThreadedStepConfig {

    @Bean
    public Job multiThreadJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("multiThreadJob", jobRepository)
            .start(step)
            .build();
    }

    @Bean
    public Step step(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     ItemReader<Order> reader,
                     ItemProcessor<Order, ProcessedOrder> processor,
                     ItemWriter<ProcessedOrder> writer,
                     TaskExecutor taskExecutor) {

        return new StepBuilder("multiThreadedStep", jobRepository)
            .<Order, ProcessedOrder>chunk(200, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .taskExecutor(taskExecutor)
            .throttleLimit(6)          // 最大并发 chunk 数，默认 4
            .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("batch-chunk-");
        executor.initialize();
        return executor;
    }
}
```

### 13.2.3 底层架构：`ChunkOrientedTasklet` 执行流程

多线程 Step 的核心是 `ChunkOrientedTasklet`，它在 `RepeatTemplate` 的驱动下循环执行 chunk。

```
AbstractStep.doExecute()
│
├── StepExecution 初始化（状态 = STARTED）
├── BeforeStep() 监听器
│
├── ChunkOrientedTasklet.execute()
│   ├── RepeatTemplate 循环:
│   │   ├── ChunkProvider.provide()
│   │   │   ├── 调用 ItemReader.read() 收集 commit-interval 个 item
│   │   │   └── 返回 Chunk<I>
│   │   │
│   │   ├── ChunkProcessor.process(Chunk<I>)
│   │   │   ├── 调用 ItemProcessor.process() 处理每个 item
│   │   │   └── 返回 Chunk<O>
│   │   │
│   │   ├── ItemWriter.write(Chunk<O>)
│   │   │
│   │   └── 提交事务（PlatformTransactionManager.commit()）
│   │
│   └── 直到 RepeatStatus.FINISHED 或数据耗尽
│
├── AfterStep() 监听器
├── StepExecution 持久化（状态 = COMPLETED）
└── 返回 ExitStatus
```

**多线程的切入位置**：在 Step 内部，每个 `doExecute()` 的执行被委托给 `TaskExecutor`。但这里的关键是——并不是 Step 本身被并行化，而是 `chunk` 的重复循环被并行化。

### 13.2.4 内部实现源码流程（简化）

```
SimpleStepExecutorHandler.doExecute()
│
├── 创建 StepExecution
├── 将 Step 包装为 Callable
├── taskExecutor.execute(callableStep)  ← 线程池执行
│
└── 每个线程独立执行:
    ├── TransactionTemplate.execute()
    │   ├── ChunkProvider.provide()     ← 加锁的 reader
    │   ├── ChunkProcessor.process()
    │   └── ItemWriter.write()
    └── 事务提交/回滚
```

### 13.2.5 线程模型图解

```
                    ┌─────────────────────────────────────────┐
                    │           SimpleStepExecutorHandler      │
                    │  (多线程 Step 的执行器)                   │
                    └────────────┬────────────────────────────┘
                                 │
                ┌───────────────┼───────────────┐
                │               │               │
         ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
         │ Thread-1    │ │ Thread-2    │ │ Thread-3    │
         │             │ │             │ │             │
         │  TX-1       │ │  TX-2       │ │  TX-3       │
         │  ┌───────┐  │ │  ┌───────┐  │ │  ┌───────┐  │
         │  │ Read  │  │ │  │ Read  │  │ │  │ Read  │  │
         │  │   ↓   │  │ │  │   ↓   │  │ │  │   ↓   │  │
         │  │Process│  │ │  │Process│  │ │  │Process│  │
         │  │   ↓   │  │ │  │   ↓   │  │ │  │   ↓   │  │
         │  │ Write │  │ │  │ Write │  │ │  │ Write │  │
         │  └───────┘  │ │  └───────┘  │ │  └───────┘  │
         │    C-1      │ │    C-2      │ │    C-3      │
         └─────────────┘ └─────────────┘ └─────────────┘
                              CHUNK
             ┌───────────────┼───────────────┐
             │    200 items  │   200 items    │   200 items
        ┌────▼────┐   ┌────▼────┐   ┌────▼────┐
        │  Item   │   │  Item   │   │  Item   │
        │  Reader │   │  Reader │   │  Reader │   ← 必须线程安全！
        └─────────┘   └─────────┘   └─────────┘
```

**关键理解**：在多线程 Step 中，`ItemReader` 是所有线程**共享**的实例。如果 Reader 是有状态的（如 `JdbcCursorItemReader` 维护一个 `ResultSet` 游标），多个线程同时调用 `read()` 会导致数据混乱。

### 13.2.6 `throttleLimit` 限流机制

`throttleLimit` 控制同时执行的 chunk 数量上限，避免过多线程耗尽数据库连接池或内存。

```java
// 默认值
public static final int DEFAULT_THROTTLE_LIMIT = 4;
```

**内部实现**：`ThrottleLimitResultProvider` 配合 `Semaphore` 实现限流。

```
throttleLimit = 4 时:
                    ┌──────────────────────┐
                    │  Semaphore(4)         │ ← 只有 4 个许可
                    └──────────────────────┘
                          │    │    │    │
                    ┌─────┘    │    │    └─────┐
                    │          │    │          │
              ┌─────▼──┐ ┌────▼───┐ ┌───▼────┐ ┌───▼────┐
              │ Chunk 1│ │ Chunk 2│ │ Chunk 3│ │ Chunk 4│ ← 并发执行
              └────────┘ └────────┘ └────────┘ └────────┘
                                                    │
             当某个 chunk 完成后:                     │
             释放 Semaphore 许可 →                   │
             允许 Chunk 5 开始执行              ┌────▼───┐
                                                │ Chunk 5│
                                                └────────┘
```

---

## 13.3 `ItemReader` 线程安全分析

### 13.3.1 各 Reader 线程安全对照表

| Reader 类型 | 线程安全 | 原因 | 解决方案 |
|-------------|---------|------|---------|
| `JdbcCursorItemReader` | ❌ 否 | 共享 `ResultSet` 游标 | 使用 `JdbcPagingItemReader` |
| `JpaCursorItemReader` | ❌ 否 | 共享 `EntityManager` + 游标 | 使用 `JpaPagingItemReader` |
| `FlatFileItemReader` | ❌ 否 | 共享文件指针 | 使用 `SynchronizedItemStreamReader` 包装 |
| `StaxEventItemReader` | ❌ 否 | 共享 XML 流解析器 | 使用包装器同步 |
| `JdbcPagingItemReader` | ✅ 是 | 每页独立查询 | 无需处理 |
| `JpaPagingItemReader` | ✅ 是 | 每页独立查询 + 新 `EntityManager` | 无需处理 |
| `MongoItemReader` | ✅ 是 | 每页独立查询 | 无需处理 |
| `RepositoryItemReader` | ⚠️ 条件 | Paging 模式安全 | Cursor 模式不安全 |

### 13.3.2 `SynchronizedItemStreamReader` 包装器

对于非线程安全的 Reader，可以使用同步包装器：

```java
@Bean
public ItemReader<Customer> reader() {
    // 原始非线程安全 Reader
    FlatFileItemReader<Customer> delegate = new FlatFileItemReaderBuilder<Customer>()
        .name("customerReader")
        .resource(new FileSystemResource("input/customers.csv"))
        .delimited()
        .names("id", "name", "email")
        .targetType(Customer.class)
        .build();

    // 用同步包装器包裹
    SynchronizedItemStreamReader<Customer> syncReader =
        new SynchronizedItemStreamReader<>();
    syncReader.setDelegate(delegate);

    return syncReader;
}
```

**内部原理**：`SynchronizedItemStreamReader` 对 `read()` 方法加 `synchronized` 关键字：

```java
// 简化源码
public class SynchronizedItemStreamReader<T> implements ItemStreamReader<T> {
    private ItemStreamReader<T> delegate;

    @Override
    public synchronized T read() throws Exception {
        return delegate.read();  // 同一时刻只有一个线程能读
    }
}
```

> **注意**：同步包装器虽然保证了线程安全，但也**破坏了并行性**——所有线程在 Reader 处串行等待，形成"读瓶颈"。这也是为什么多线程 Step 最适合 **Reader I/O 快 + Processor/Writer I/O 慢**的场景。

### 13.3.3 `JdbcPagingItemReader` 线程安全原理

`JdbcPagingItemReader` 每读取一页数据都执行独立的 SQL 查询，而不是维护一个共享游标：

```java
// 简化逻辑
public class JdbcPagingItemReader<T> extends AbstractPagingItemReader<T> {

    @Override
    protected void doReadPage() {
        // 每个线程调用时，执行独立查询
        // 使用页码 + 页大小计算 OFFSET 和 LIMIT
        List<T> results = jdbcTemplate.query(
            getSql(),            // SELECT * FROM table ORDER BY id
            new Object[]{ getPage() * getPageSize(), getPageSize() },
            rowMapper
        );
        results.forEach(this::addToResults);
    }
}
```

多线程场景下的关键问题：**多个线程读取同一页**会导致数据重复/丢失。解决方案：
1. 使用 `saveState(false)` 关闭状态跟踪（框架不需要保持页码一致性，因为各线程自己管自己的 chunk）
2. 确保 Step 重启时可以从头开始

---

## 13.4 `AsyncItemProcessor` / `AsyncItemWriter` 深度解析

### 13.4.1 概念

`AsyncItemProcessor` 和 `AsyncItemWriter` 来自 `spring-batch-integration` 模块，实现**仅 Processor 阶段异步并行**的模式。Reader 和 Writer 保持在单个事务线程中。

### 13.4.2 配置示例

```xml
<!-- 需要额外引入 -->
<dependency>
    <groupId>org.springframework.batch</groupId>
    <artifactId>spring-batch-integration</artifactId>
</dependency>
```

```java
@Configuration
public class AsyncProcessorConfig {

    @Bean
    public Job asyncJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("asyncProcessingJob", jobRepository)
            .start(step)
            .build();
    }

    @Bean
    public Step step(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     ItemReader<Order> reader,
                     AsyncItemProcessor<Order, ProcessedOrder> asyncProcessor,
                     AsyncItemWriter<ProcessedOrder> asyncWriter) {

        return new StepBuilder("asyncStep", jobRepository)
            // ★ 注意：泛型是 <Order, Future<ProcessedOrder>>
            .<Order, Future<ProcessedOrder>>chunk(100, transactionManager)
            .reader(reader)
            .processor(asyncProcessor)     // 返回 Future<ProcessedOrder>
            .writer(asyncWriter)           // 解包 Future
            .build();
    }

    @Bean
    public AsyncItemProcessor<Order, ProcessedOrder> asyncProcessor(
            ItemProcessor<Order, ProcessedOrder> delegate,
            TaskExecutor taskExecutor) {

        AsyncItemProcessor<Order, ProcessedOrder> processor =
            new AsyncItemProcessor<>();
        processor.setDelegate(delegate);
        processor.setTaskExecutor(taskExecutor);
        return processor;
    }

    @Bean
    public AsyncItemWriter<ProcessedOrder> asyncWriter(
            ItemWriter<ProcessedOrder> delegate) {

        AsyncItemWriter<ProcessedOrder> writer = new AsyncItemWriter<>();
        writer.setDelegate(delegate);
        return writer;
    }

    @Bean
    public ItemProcessor<Order, ProcessedOrder> realProcessor() {
        return order -> {
            // 模拟耗时处理（外部 API 调用、复杂计算等）
            Thread.sleep(100);
            return new ProcessedOrder(order.getId(), "Processed:" + order.getData());
        };
    }

    @Bean
    public TaskExecutor processorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-process-");
        executor.initialize();
        return executor;
    }
}
```

### 13.4.3 内部实现源码分析

**`AsyncItemProcessor` 核心逻辑**：

```java
// 源码分析（简化）
public class AsyncItemProcessor<I, O> implements ItemProcessor<I, Future<O>> {

    private ItemProcessor<I, O> delegate;
    private TaskExecutor taskExecutor;

    @Override
    public Future<O> process(I item) throws Exception {

        // 为每个 item 创建一个 FutureTask
        FutureTask<O> future = new FutureTask<>(() -> delegate.process(item));

        // 提交到线程池异步执行
        taskExecutor.execute(future);  // ← 真正的并行点

        // 立即返回 Future（不阻塞当前线程）
        return future;
    }
}
```

**`AsyncItemWriter` 核心逻辑**：

```java
// 源码分析（简化）
public class AsyncItemWriter<O> implements ItemWriter<Future<O>> {

    private ItemWriter<O> delegate;

    @Override
    public void write(Chunk<? extends Future<O>> futures) throws Exception {

        // 1. 逐个 Future.get() 等待所有异步任务完成
        List<O> results = new ArrayList<>();
        for (Future<O> future : futures) {
            results.add(future.get());  // ← 阻塞等待
        }

        // 2. 将解包后的结果批量交给真实 Writer
        delegate.write(new Chunk<>(results));
    }
}
```

### 13.4.4 时序图

```
Reader Thread                      Thread Pool                      Writer Thread
     │                                 │                                │
     │── read() → item1 ───────────────│────────────────────────────────│
     │  asyncProcessor.process(item1)  │                                │
     │    └── submit to pool ─────────>│── process(item1) ...          │
     │    └── return Future<O1>        │    耗时计算中...               │
     │                                 │                                │
     │── read() → item2 ───────────────│────────────────────────────────│
     │  asyncProcessor.process(item2)  │                                │
     │    └── submit to pool ─────────>│── process(item2) ...          │
     │    └── return Future<O2>        │    耗时计算中...               │
     │                                 │                                │
     │  ... 继续读取直到 chunk 满 ...   │    (并行处理 10 个 item)       │
     │                                 │                                │
     │─────────────────────────────────│────────────────────────────────│
     │                                 │                    Writer 开始 │
     │                                 │    <── Future<O1>.get() ──────│
     │                                 │    <── Future<O2>.get() ──────│
     │                                 │    <── Future<O3>.get() ──────│
     │                                 │    <── ...                    │
     │                                 │    └── delegate.write(list)   │
     │                                 │                                │
```

> **重要**：在多线程 Step 中不要再额外使用 `AsyncItemProcessor`。两者叠加会导致两层并行——Chunk 级并行 + Item 级并行，通常得不偿失且难以调试。

---

## 13.5 分区（Partitioning）底层原理

### 13.5.1 概念

分区是将数据切分成多个独立的分片，每个分片由一个独立的 StepExecution 处理。这是 Spring Batch 中最强大、最灵活的并行方式。

```
         ┌────────────────────────────────────────┐
         │            Master Step                  │
         │                                        │
         │  StepExecutionSplitter                 │
         │    ├── 创建 4 个 StepExecution          │
         │    └── 每个携带分区参数                  │
         └────────┬──────────────┬───────────────┘
                  │              │
        ┌─────────▼──────┐ ┌────▼──────────┐
        │ Worker Step-1  │ │ Worker Step-2 │  ...
        │                │ │               │
        │ DB: id 1-250   │ │ DB: id 251-500│
        │ ┌──────┐       │ │ ┌──────┐      │
        │ │ R→P→W│       │ │ │ R→P→W│      │
        │ └──────┘       │ │ └──────┘      │
        │ TX-1           │ │ TX-2          │
        └────────────────┘ └───────────────┘
```

### 13.5.2 配置示例

```java
@Configuration
public class PartitioningJobConfig {

    @Bean
    public Job partitionJob(JobRepository jobRepository, Step masterStep) {
        return new JobBuilder("partitionJob", jobRepository)
            .start(masterStep)
            .build();
    }

    // ========== Master Step ==========
    @Bean
    public Step masterStep(JobRepository jobRepository,
                           PartitionHandler partitionHandler) {

        return new StepBuilder("masterStep", jobRepository)
            .partitioner("workerStep", partitioner())  // 分区器
            .partitionHandler(partitionHandler)
            .build();
    }

    // ========== Worker Step（每个分区执行的业务逻辑） ==========
    @Bean
    public Step workerStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<Customer> reader,
                           ItemWriter<Customer> writer) {

        return new StepBuilder("workerStep", jobRepository)
            .<Customer, Customer>chunk(500, transactionManager)
            .reader(reader)    // ★ 每个 Worker 有自己的 Reader 实例
            .writer(writer)
            .build();
    }

    // ========== 分区器 ==========
    @Bean
    public Partitioner partitioner() {
        return new ColumnRangePartitioner("customer", "id", 1L, 1000000L);
    }

    // ========== 分区处理器 ==========
    @Bean
    public PartitionHandler partitionHandler(
            @Qualifier("workerStep") Step workerStep,
            TaskExecutor taskExecutor) {

        TaskExecutorPartitionHandler handler =
            new TaskExecutorPartitionHandler();
        handler.setStep(workerStep);
        handler.setTaskExecutor(taskExecutor);
        handler.setGridSize(8);  // 8 个分区
        return handler;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("partition-");
        executor.initialize();
        return executor;
    }
}
```

### 13.5.3 自定义分区器

```java
public class ColumnRangePartitioner implements Partitioner {

    private String table;
    private String column;
    private Long minValue;
    private Long maxValue;

    // 构造函数、setter 略

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>(gridSize);

        long range = (maxValue - minValue) / gridSize;
        long start = minValue;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext ctx = new ExecutionContext();
            ctx.putLong("startValue", start);
            ctx.putLong("endValue", start + range);
            ctx.putString("table", table);
            ctx.putString("column", column);

            partitions.put("partition-" + i, ctx);
            start += range + 1;

            log.info("Created partition {}: {} - {}",
                "partition-" + i,
                ctx.getLong("startValue"),
                ctx.getLong("endValue"));
        }

        return partitions;
    }
}
```

### 13.5.4 Worker 中读取分区参数

```java
@StepScope  // ★ 关键：StepScope 确保每个分区创建独立的 Reader 实例
@Bean
public JdbcPagingItemReader<Customer> pagingReader(
        @Value("#{stepExecutionContext['startValue']}") Long startValue,
        @Value("#{stepExecutionContext['endValue']}") Long endValue,
        DataSource dataSource) {

    // 每个 Worker 只读自己的数据范围
    Map<String, Object> parameterValues = new HashMap<>();
    parameterValues.put("startValue", startValue);
    parameterValues.put("endValue", endValue);

    return new JdbcPagingItemReaderBuilder<Customer>()
        .name("customerPagingReader")
        .dataSource(dataSource)
        .selectClause("SELECT *")
        .fromClause("FROM customer")
        .whereClause("WHERE id >= :startValue AND id <= :endValue")
        .parameterValues(parameterValues)
        .pageSize(500)
        .rowMapper((rs, rowNum) -> {
            Customer c = new Customer();
            c.setId(rs.getLong("id"));
            c.setName(rs.getString("name"));
            return c;
        })
        .build();
}
```

### 13.5.5 `TaskExecutorPartitionHandler` 内部源码分析

```java
// 简化源码
public class TaskExecutorPartitionHandler extends AbstractPartitionHandler {

    private TaskExecutor taskExecutor;
    private Step step;

    @Override
    public Collection<StepExecution> handle(StepExecutionManager stepExecutionManager,
                                            StepExecution masterStepExecution,
                                            Set<StepExecution> partitionStepExecutions)
            throws Exception {

        // 1. 创建 CountDownLatch 等待所有分区完成
        CountDownLatch latch = new CountDownLatch(partitionStepExecutions.size());

        // 2. 为每个分区创建 Runnable，提交到线程池
        for (StepExecution partitionStepExecution : partitionStepExecutions) {

            // 每个分区在一个独立线程中执行
            taskExecutor.execute(() -> {
                try {
                    // 执行 Worker Step
                    step.execute(partitionStepExecution);
                } catch (Exception e) {
                    // 记录分区执行异常
                    partitionStepExecution.setStatus(BatchStatus.FAILED);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 3. 等待所有分区完成
        latch.await();

        // 4. 聚合结果返回
        return partitionStepExecutions;
    }
}
```

**关键设计要点**：

| 组件 | 职责 |
|------|------|
| `StepExecutionSplitter` | 根据 Partitioner 和 gridSize 创建 `Set<StepExecution>` |
| `TaskExecutorPartitionHandler` | 将每个 `StepExecution` 提交到线程池执行 |
| `Partitioner` | 定义数据切分逻辑，返回 `Map<String, ExecutionContext>` |
| `@StepScope` | 确保每个分区创建独立的 Bean 实例，避免线程间状态共享 |

---

## 13.6 Spring Batch 6.0 新的 Producer-Consumer 模型

### 13.6.1 旧模型的局限性

在 Spring Batch 5.x 及之前的版本中，多线程 Step 使用"并行迭代"模型——每个线程独立竞争读取数据。这导致：

1. **状态同步开销大**：Reader 需要 `synchronized` 或原子操作
2. **事务语义模糊**：各线程的事务独立但共享 Reader 状态
3. **无背压机制**：无法限制生产速度，可能导致内存溢出

### 13.6.2 新 Producer-Consumer 模型（6.0+）

Spring Batch 6.0.0-M3 引入全新的**生产者-消费者**模型：

```
旧模型（5.x）:
Thread-1: [锁 → 读] → [Process] → [Write] → [锁 → 读] → ...
Thread-2: [锁 → 读] → [Process] → [Write] → [锁 → 读] → ...
           ▲                             ▲
           └── 锁竞争严重                 └── Writer 各自为政

新模型（6.0+）:
Producer Thread: [Read] → queue → [Read] → queue → [Read] → queue → ...
                           │          │          │
                    ┌──────┘    ┌─────┘    ┌────┘
                    ▼           ▼          ▼
Consumer Pool:  [Process]   [Process]   [Process]
                    │           │          │
                    ▼           ▼          ▼
              [Write] ←── 合并结果 ──→ [Write]
                    ▲
                    └── 写入完成后再通知 Producer 继续
```

**核心特性**：

| 特性 | 说明 |
|------|------|
| **有界内部队列** | 生产者将 item 放入有界队列，队列满时自动阻塞 |
| **内置背压** | 消费者处理速度慢 → 队列满 → 生产者暂停读取 |
| **生产者暂停机制** | 当 chunk 准备好写入时，生产者暂停直到写入完成，然后恢复 |
| **更清晰的事务语义** | 读写分离，事务边界更明确 |
| **`StoppableStep`** | 新的优雅停止接口 |

### 13.6.3 新模型伪代码

```java
// Spring Batch 6.0 新模型（概念伪代码）
public class ProducerConsumerChunkProvider<I> implements ChunkProvider<I> {

    private final BlockingQueue<I> queue;    // 有界阻塞队列
    private final ItemReader<I> reader;
    private final int chunkSize;

    public Chunk<I> provide() {
        Chunk<I> chunk = new Chunk<>();

        // 从队列中获取 item
        while (chunk.size() < chunkSize) {
            I item = queue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if (item == null) break;  // 队列空且生产者已完成
            chunk.add(item);
        }

        return chunk;
    }

    // 生产者线程
    private class Producer implements Runnable {
        @Override
        public void run() {
            I item;
            while ((item = reader.read()) != null) {
                queue.put(item);  // 队列满时自动阻塞 ← 背压!
            }
        }
    }
}
```

> 注意：Spring Batch 6.0 仍处于 M3 阶段，API 和生产使用可能有变化。生产环境建议继续使用 Spring Batch 5.x 的成熟模型。

---

## 13.7 事务与并发控制

### 13.7.1 事务边界对比

| 并行模式 | 事务边界 | 事务数量 |
|---------|---------|---------|
| 单线程 Step | 每 chunk 一个事务 | N 个（N = chunk 数） |
| 多线程 Step | 每 chunk 独立事务（跨线程） | N × 线程数 |
| Async Processor | 整个 Step 一个事务 | 1 个 |
| 分区 | 每分区独立 Step 事务 | M × N（M = 分区数，N = 每分区 chunk 数） |

### 13.7.2 多线程 Step 的事务配置

```java
@Bean
public Step multiThreadedStep(JobRepository jobRepository,
                              DataSource dataSource,
                              ...) {

    // 创建独立的事务管理器（每个 chunk 的隔离级别可独立控制）
    DataSourceTransactionManager txManager =
        new DataSourceTransactionManager(dataSource);

    // 设置隔离级别
    txManager.setIsolationLevelName(
        TransactionDefinition.ISOLATION_READ_COMMITTED);

    return new StepBuilder("multiThreadedStep", jobRepository)
        .<Order, ProcessedOrder>chunk(200, txManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .taskExecutor(taskExecutor)
        .throttleLimit(6)
        .build();
}
```

**隔离级别建议**：

| 隔离级别 | 多线程场景适用性 | 说明 |
|---------|-----------------|------|
| `READ_UNCOMMITTED` | ❌ 不推荐 | 脏读导致数据不一致 |
| `READ_COMMITTED` | ✅ 推荐 | 防止脏读，性能好 |
| `REPEATABLE_READ` | ⚠️ 谨慎 | 防止不可重复读，可能降低并发 |
| `SERIALIZABLE` | ❌ 不推荐 | 锁竞争严重，失去并行意义 |

### 13.7.3 多线程事务隔离示意图

```
时间 →
Thread-1: [TX-1: R→P→W]─────[TX-3: R→P→W]─────[TX-5: R→P→W]
Thread-2:     [TX-2: R→P→W]─────[TX-4: R→P→W]─────[TX-6: R→P→W]

数据库状态:
DB:     v1────v2────v3────v4────v5────v6────v7────v8────
        TX-1    TX-2    TX-1    TX-2    TX-3    TX-4
        提交    提交    提交    提交    提交    提交

★ 关键：TX-1 看不到 TX-2 未提交的数据（READ_COMMITTED）
         TX-3 能看到 TX-1 和 TX-2 已提交的数据
```

---

## 13.8 实战：订单处理系统多线程优化

### 13.8.1 场景描述

处理 100 万条订单记录，每条需要：
1. 读取订单（DB I/O）
2. 调用外部风控 API（HTTP I/O，耗时 ~200ms）
3. 写入处理结果（DB I/O）

### 13.8.2 性能瓶颈分析

```
单线程 Step 性能:
1000000 条 × (10ms 读 + 200ms API + 10ms 写) / chunk 500
= 220秒 总耗时（约 3.7 分钟）

但 API 调用占 90% 时间 → Processor 是瓶颈！
```

### 13.8.3 方案一：多线程 Step

```java
@Bean
public Step multiThreadedStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              ItemReader<Order> reader,
                              ItemProcessor<Order, ProcessedOrder> processor,
                              ItemWriter<ProcessedOrder> writer) {

    JdbcPagingItemReader<Order> pagingReader = ...;  // 线程安全

    return new StepBuilder("multiThreadedStep", jobRepository)
        .<Order, ProcessedOrder>chunk(500, txManager)
        .reader(pagingReader)    // 线程安全
        .processor(processor)    // 无状态
        .writer(writer)          // JdbcBatchItemWriter 线程安全
        .taskExecutor(taskExecutor)
        .throttleLimit(8)
        .build();
}
```

**预估性能**：220s / 8 = ~28 秒（I/O 密集型，近似线性加速）

### 13.8.4 方案二：AsyncItemProcessor（Processor 瓶颈场景）

```java
@Bean
public Step asyncStep(JobRepository jobRepository,
                      PlatformTransactionManager txManager,
                      ItemReader<Order> reader,
                      AsyncItemProcessor<Order, ProcessedOrder> asyncProcessor,
                      AsyncItemWriter<ProcessedOrder> asyncWriter) {

    return new StepBuilder("asyncStep", jobRepository)
        .<Order, Future<ProcessedOrder>>chunk(100, txManager)
        .reader(reader)
        .processor(asyncProcessor)
        .writer(asyncWriter)
        .build();
}

@Bean
public AsyncItemProcessor<Order, ProcessedOrder> asyncProcessor(
        ItemProcessor<Order, ProcessedOrder> delegate) {

    // 使用 20 个线程处理 API 调用
    AsyncItemProcessor<Order, ProcessedOrder> processor =
        new AsyncItemProcessor<>();
    processor.setDelegate(delegate);
    processor.setTaskExecutor(asyncTaskExecutor());
    return processor;
}

@Bean
public TaskExecutor asyncTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(20);
    executor.setMaxPoolSize(40);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("api-call-");
    executor.initialize();
    return executor;
}
```

**预估性能**：1000000 × (10ms 读 + 200ms/20 + 10ms 写) / 100 ≈ ~20 秒

### 13.8.5 方案三：分区（数据可切分场景）

```java
// 8 个分区，每分区 12.5 万条
// 各分区独立 Step、独立 Reader、独立事务

@Bean
public Step workerStep(JobRepository jobRepository,
                       PlatformTransactionManager txManager,
                       @Value("#{stepExecutionContext['minId']}") Long minId,
                       @Value("#{stepExecutionContext['maxId']}") Long maxId,
                       DataSource dataSource) {

    JdbcPagingItemReader<Order> reader = new JdbcPagingItemReaderBuilder<Order>()
        .name("orderReader")
        .dataSource(dataSource)
        .selectClause("SELECT *")
        .fromClause("FROM orders")
        .whereClause("WHERE id >= ? AND id <= ?")
        .parameterValues(Map.of("minId", minId, "maxId", maxId))
        .pageSize(500)
        .rowMapper(...)
        .build();

    return new StepBuilder("workerStep", jobRepository)
        .<Order, ProcessedOrder>chunk(500, txManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();  // 单线程 Step，但每个分区独立执行
}
```

### 13.8.6 方案对比总结

| 指标 | 单线程 Step | 多线程 Step | AsyncProcessor | 分区 |
|------|------------|------------|---------------|------|
| **预估耗时** | 220s | ~28s | ~20s | ~30s |
| **配置复杂度** | 低 | 低 | 中 | 中高 |
| **线程安全性** | 天然安全 | 需确认 Reader 安全 | 安全 | 天然安全 |
| **资源消耗** | 1 线程 | 8 线程 | 20+ 线程 | 8 Step × 1 线程 |
| **重启恢复** | 断点续跑 | 不支持 (saveState=false) | 精细控制 | 精细控制 |
| **适用场景** | 所有 | I/O 密集型 | Processor 瓶颈 | 数据可切分 |

---

## 13.9 常见陷阱与最佳实践

### 13.9.1 Hibernate Session 在多线程中关闭

**问题**：多线程 Step 中使用 `RepositoryItemReader` + `JpaItemWriter`，Processor 中懒加载关联数据时抛出 `LazyInitializationException`。

```
原因:
Thread-1: [reader.read() → Session打开] → [processor → Session已关闭!]
                                                             ↑
                                            Session 绑定在读线程，
                                            处理线程无法访问
```

**解决方案**：

```java
// ❌ 错误：使用 RepositoryItemReader（不推荐多线程）
@Bean
public RepositoryItemReader<Order> reader(OrderRepository repo) {
    return new RepositoryItemReaderBuilder<Order>()
        .repository(repo)
        .methodName("findAll")
        .build();
}

// ✅ 正确：使用 JpaPagingItemReader（每页新 EntityManager）
@Bean
public JpaPagingItemReader<Order> reader(EntityManagerFactory emf) {
    return new JpaPagingItemReaderBuilder<Order>()
        .name("orderReader")
        .entityManagerFactory(emf)
        .queryString("SELECT o FROM Order o JOIN FETCH o.items")
        .pageSize(500)
        .build();
}
```

> **最佳实践**：在多线程 Step 中，始终使用 JOIN FETCH 或 EntityGraph 预先加载所有需要的关联数据，避免懒加载。

### 13.9.2 `saveState(false)` 的必要性

在多线程 Step 中，`ItemReader` 的状态跟踪（ExecutionContext 中的 `read.count`）是线程不安全的，多个线程同时更新会导致状态损坏。

```java
return new StepBuilder("multiThreadedStep", jobRepository)
    .<Order, ProcessedOrder>chunk(500, transactionManager)
    .reader(reader)
    .processor(processor)
    .writer(writer)
    .readerIsTransactionalQueue()  // ★ 等效于 saveState(false)
    .taskExecutor(taskExecutor)
    .throttleLimit(8)
    .build();
```

**后果**：
- **`saveState(true)`（默认）**：多线程写入 ExecutionContext 导致数据竞争，重启时恢复错误的进度
- **`saveState(false)`**：不记录读取进度，重启时**从头开始**（作业必须幂等）

### 13.9.3 `throttleLimit` 与线程池大小关系

```
线程池大小（corePoolSize）≥ throttleLimit + 1

Explanation:
- throttleLimit 个线程用于执行 chunk
- 额外 1 个线程处理其他开销（Step 管理、日志等）
- 若 corePoolSize < throttleLimit，实际并发数会受限

示例:
- throttleLimit = 8 → corePoolSize ≥ 9
- throttleLimit = 16 → corePoolSize ≥ 17
```

### 13.9.4 数据库连接池配置

多线程 Step 需要足够的数据库连接：

```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=20
# 公式: throttleLimit × (每个 chunk 占用连接数) + 预留连接
# 通常: throttleLimit × 2 + 5
```

### 13.9.5 最佳实践清单

```
多线程 Step 配置检查清单:
□ ItemReader 是线程安全的（JdbcPagingItemReader / JpaPagingItemReader）
□ 已设置 readerIsTransactionalQueue() 或 saveState(false)
□ throttleLimit ≤ TaskExecutor corePoolSize - 1
□ 数据库连接池 ≥ throttleLimit × 2 + 5
□ Step 重启幂等（重新执行不产生重复数据）
□ ItemProcessor 无状态（不持有实例变量）
□ 已在分段 SQL 中使用 ORDER BY（保证分页一致性）
□ 使用 JOIN FETCH 避免懒加载异常
```

---

## 13.10 总结

```
多线程处理核心原则:
                     ┌───────────────────────────┐
                     │   选择策略的决策树           │
                     └───────────────────────────┘
                                │
                     Processor 是瓶颈吗？
                    ┌─────┴─────┐
                   YES          NO
                    │            │
              ┌─────▼────┐  I/O 密集 + Reader 线程安全？
              │ 使用      │  ┌─────┴─────┐
              │AsyncItem  │ YES          NO
              │Processor  │  │            │
              │+ Writer   │  │       ┌────▼────┐
              └───────────┘  │       │ 数据可   │
                             │       │ 切分？   │
                        ┌────▼──┐    ┌────┴────┐
                        │多线程  │   YES       NO
                        │Step   │    │          │
                        └───────┘ ┌──▼──┐  ┌───▼────┐
                                  │分区  │  │ 单线程  │
                                  │     │  │ Step    │
                                  └─────┘  └────────┘

核心机制对比:
┌──────────────────────────────────────────────┐
│ 多线程 Step  =  同一 Step 内多 chunk 并行       │
│ AsyncItem    =  Processor 多 item 并行         │
│ 分区          =  多独立 Step 并行                │
│ 远程分块      =  多 JVM 并行                     │
└──────────────────────────────────────────────┘

线程安全层级:
Reader   → 必须线程安全（或使用同步包装器）          │ 高风险
Processor → 必须无状态                              │ 中风险
Writer    → 通常安全（JdbcBatchItemWriter 等）      │ 低风险

性能公式:
单线程耗时 = N × (time_read + time_process + time_write) / chunk_size
多线程耗时 ≈ 单线程耗时 / min(throttleLimit, 线程池大小)
```

### 13.10.1 各策略选择速查表

| 你遇到的情况 | 推荐策略 | 原因 |
|-------------|---------|------|
| 读快、处理慢、写快 | 多线程 Step | 读阶段不成为瓶颈，多 chunk 并行 |
| 处理超慢（外部 API） | AsyncItemProcessor | 每个 item 独立并行处理 |
| 数据量大且可切分 | 分区 | 数据级并行，天然线程安全 |
| 需要跨机器分发 | 远程分块 | 突破单机资源限制 |
| 处理逻辑简单、IO 密集 | 多线程 Step | 配置简单、收益明显 |
| 需要精确断点续跑 | 分区 | 每分区独立管理状态 |

---

## 13.11 实战分析：多文件并发 CPU 高问题

真实生产场景分析已移至独立文档：

[第14章：实战分析 — 多文件并发 CPU 高问题排查与优化](14-multifile-concurrency-analysis.md)

内容包括：
- 14 种文件通过 API 同时触发 Job 的 CPU 高根因排查
- `SimpleAsyncTaskExecutor` 源码缺陷分析
- 不能改 API 层的约束下，如何通过 Step 配置内部优化
- 共享 `ThreadPoolTaskExecutor` 方案 vs 单线程 Step 方案

---

**扩展阅读**：
- 第4章 [Step Types](04-step-types.md) — Chunk-oriented Step 基础
- 第11章 [Error Handling](11-error-handling.md) — 多线程下的容错处理
- 第12章 [Advanced Features](12-advanced-features.md) — 分区详细配置
- [Spring Batch 6.0 官方文档 - 并发模型](https://docs.spring.io/spring-batch/reference/6.0-SNAPSHOT/scalability.html)
- [Spring Batch Integration - Async Processing](https://docs.spring.io/spring-batch/reference/spring-batch-integration/asynchronous-processing.html)
