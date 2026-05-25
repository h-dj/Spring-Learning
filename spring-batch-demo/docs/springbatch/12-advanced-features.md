# Chapter 12: Advanced Features

## Overview
Master advanced Spring Batch features including partitioning, parallel processing, remote chunking, and scaling.

## 12.1 Partitioning Overview

```
Partitioning Strategy
├── PartitionHandler (Distributes work)
├── Partitioner (Creates partitions)
├── GridSize (Number of partitions)
└── PartitionedStep (Executes in parallel)
```

## 12.2 partitioning

### TaskExecutorPartitionHandler
```java
@Configuration
public class PartitioningJobConfig {

    @Bean
    public Job partitionJob(JobRepository jobRepository,
                            Step managerStep) {
        return new JobBuilder("partitionJob", jobRepository)
            .start(managerStep)
            .build();
    }

    @Bean
    public Step managerStep(JobRepository jobRepository,
                           PlatformTransactionManager txManager,
                           ItemReader<String> reader,
                           ItemProcessor<String, String> processor,
                           ItemWriter<String> writer,
                           PartitionHandler partitionHandler) {

        return new StepBuilder("managerStep", jobRepository)
            .partitioner("workerStep", partitioner())
            .partitionHandler(partitionHandler)
            .build();
    }

    @Bean
    public Partitioner partitioner() {
        return new ColumnRangePartitioner("customers", "id", 1L, 1000000L);
    }

    @Bean
    public PartitionHandler partitionHandler(
            @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor,
            Step workerStep) {

        TaskExecutorPartitionHandler handler =
            new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(taskExecutor);
        handler.setStep(workerStep);
        handler.setGridSize(4);
        return handler;
    }

    @Bean("batchTaskExecutor")
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Partition-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Step workerStep(JobRepository jobRepository,
                          PlatformTransactionManager txManager,
                          ItemReader<String> reader,
                          ItemWriter<String> writer) {

        return new StepBuilder("workerStep", jobRepository)
            .<String, String>chunk(100, txManager)
            .reader(partitionReader(null))  // Uses ExecutionContext
            .writer(writer)
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<String> partitionReader(
            @Value("#{stepExecutionContext[minValue]}") Long minValue,
            @Value("#{stepExecutionContext[maxValue]}") Long maxValue) {

        return new JdbcPagingItemReaderBuilder<String>()
            .dataSource(dataSource)
            .queryProvider(pagingQueryProvider())
            .parameterValues(Map.of("minId", minValue, "maxId", maxValue))
            .rowMapper(new BeanPropertyRowMapper<>(String.class))
            .pageSize(100)
            .build();
    }
}
```

### Custom Partitioner
```java
public class DateRangePartitioner implements Partitioner {

    private final String tableName;
    private final String dateColumn;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRangePartitioner(String tableName, String dateColumn,
                                LocalDate startDate, LocalDate endDate) {
        this.tableName = tableName;
        this.dateColumn = dateColumn;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        long daysPerPartition = days / gridSize;

        LocalDate currentStart = startDate;
        for (int i = 0; i < gridSize; i++) {
            LocalDate currentEnd = (i == gridSize - 1)
                ? endDate
                : currentStart.plusDays(daysPerPartition);

            ExecutionContext context = new ExecutionContext();
            context.putString("startDate", currentStart.toString());
            context.putString("endDate", currentEnd.toString());

            partitions.put("partition_" + i, context);
            currentStart = currentEnd.plusDays(1);
        }

        return partitions;
    }
}
```

## 12.3 Parallel Processing

### SimpleAsyncTaskExecutor
```java
@Bean
public Step parallelStep(JobRepository jobRepository,
                         PlatformTransactionManager txManager,
                         Flow flowA,
                         Flow flowB) {

    Flow parallelFlow = new FlowBuilder<>("parallelFlow")
        .start(flowA)
        .split(new SimpleAsyncTaskExecutor())
        .add(flowB)
        .build();

    return new StepBuilder("parallelStep", jobRepository)
        .flow(parallelFlow)
        .build();
}
```

### ThreadPoolTaskExecutor
```java
@Configuration
public class ParallelConfig {

    @Bean(name = "parallelTaskExecutor")
    public TaskExecutor parallelTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Parallel-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean
    public Step parallelJobStep(JobRepository jobRepository,
                                PlatformTransactionManager txManager,
                                @Qualifier("parallelTaskExecutor") TaskExecutor executor,
                                Flow flow1,
                                Flow flow2) {

        Flow parallelFlow = new FlowBuilder<>("parallelFlow")
            .start(flow1)
            .split(executor)
            .add(flow2)
            .build();

        return new StepBuilder("parallelJobStep", jobRepository)
            .flow(parallelFlow)
            .build();
    }
}
```

## 12.4 Remote Chunking

### Architecture
```
Master Step ──▶ Messages ──▶ Queue ──▶ Worker Steps
                   (Chunk)          (RabbitMQ/Kafka)
```

### Configuration
```java
@Configuration
public class RemoteChunkingConfig {

    @Bean
    public Job remoteChunkJob(JobRepository jobRepository,
                             Step managerStep) {
        return new JobBuilder("remoteChunkJob", jobRepository)
            .start(managerStep)
            .build();
    }

    @Bean
    public Step managerStep(JobRepository jobRepository,
                           PlatformTransactionManager txManager,
                           ItemReader<String> reader,
                           ItemProcessor<String, String> processor,
                           ChunkHandler chunkHandler) {

        return new StepBuilder("managerStep", jobRepository)
            .<String, String>chunk(100, txManager)
            .reader(reader)
            .processor(processor)
            .chunkHandler(chunkHandler)
            .build();
    }

    @Bean
    public ChunkHandler chunkHandler(AmqpTemplate amqpTemplate) {
        RemoteChunkingWorkerStepBuilder<String, String> builder =
            new RemoteChunkingWorkerStepBuilder<>(
                "remoteChunkHandler", jobRepository, amqpTemplate);

        return builder
            .inputChannel(requests())
            .outputChannel(replies())
            .build();
    }

    @Bean
    public MessageChannel requests() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel replies() {
        return new DirectChannel();
    }
}
```

### Worker Configuration
```java
@Configuration
public class RemoteWorkerConfig {

    @Bean
    public Step workerStep(JobRepository jobRepository,
                          PlatformTransactionManager txManager,
                          ItemWriter<String> writer) {

        return new StepBuilder("workerStep", jobRepository)
            .<String, String>chunk(10, txManager)
            .reader(new StringItemReader())
            .processor(new StringProcessor())
            .writer(writer)
            .build();
    }
}
```

## 12.5 Remote Partitioning

### With Spring Integration
```java
@Configuration
public class RemotePartitioningConfig {

    @Bean
    public Job remotePartitionJob(JobRepository jobRepository,
                                  Step masterStep) {
        return new JobBuilder("remotePartitionJob", jobRepository)
            .start(masterStep)
            .build();
    }

    @Bean
    public Step masterStep(JobRepository jobRepository,
                          PartitionHandler partitionHandler,
                          Partitioner partitioner) {

        return new StepBuilder("masterStep", jobRepository)
            .partitioner("workerStep", partitioner)
            .partitionHandler(partitionHandler)
            .build();
    }

    @Bean
    public PartitionHandler partitionHandler(
            MessagingTemplate messagingTemplate) {

        return new MessagingPartitionHandler(
            messagingTemplate, "worker-requests") {
            @Override
            protected Object doSend(ExecutionContext executionContext,
                                   int gridSize) {
                // Send to remote workers
                return super.doSend(executionContext, gridSize);
            }
        };
    }
}
```

## 12.6 Multi-threaded Step

```java
@Bean
public Step multiThreadedStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              ItemReader<String> reader,
                              ItemProcessor<String, String> processor,
                              ItemWriter<String> writer) {

    return new StepBuilder("multiThreadedStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .taskExecutor(new SimpleAsyncTaskExecutor())
        .throttleLimit(4)
        .build();
}
```

## 12.7 Scaling Strategies

### Horizontal Scaling
```java
@Configuration
public class ScalingConfig {

    @Bean
    public Step scalableStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager,
                            ItemReader<String> reader,
                            ItemWriter<String> writer) {

        return new StepBuilder("scalableStep", jobRepository)
            .<String, String>chunk(100, txManager)
            .reader(reader)
            .writer(writer)
            .taskExecutor(threadPoolTaskExecutor())
            .throttleLimit(8)  // Adjust based on CPU cores
            .build();
    }

    @Bean
    public TaskExecutor threadPoolTaskExecutor() {
        int availableProcessors = Runtime.getRuntime()
            .availableProcessors();
        int poolSize = availableProcessors * 2;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Batch-");
        executor.initialize();
        return executor;
    }
}
```

### Database Optimization
```java
@Bean
public Step optimizedStep(JobRepository jobRepository,
                          PlatformTransactionManager txManager,
                          ItemReader<String> reader,
                          ItemWriter<String> writer) {

    return new StepBuilder("optimizedStep", jobRepository)
        .<String, String>chunk(1000, txManager)  // Larger chunks
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .skipLimit(100)
        .build();
}
```

## 12.8 Performance Monitoring

```java
@Component
public class PerformanceMonitoringService {

    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 60000)
    public void monitorPerformance() {
        List<JobExecution> runningExecutions = getRunningExecutions();

        for (JobExecution execution : runningExecutions) {
            for (StepExecution stepExecution :
                    execution.getStepExecutions()) {

                String stepName = stepExecution.getStepName();
                int readCount = stepExecution.getReadCount();
                int writeCount = stepExecution.getWriteCount();
                long duration = getStepDuration(stepExecution);

                // Record metrics
                meterRegistry.gauge("batch.step.read.count",
                    Tags.of("step", stepName), readCount);
                meterRegistry.gauge("batch.step.write.count",
                    Tags.of("step", stepName), writeCount);
                meterRegistry.timer("batch.step.duration",
                    Tags.of("step", stepName))
                    .record(Duration.ofMillis(duration));

                // Calculate throughput
                double throughput = duration > 0
                    ? (double) writeCount / (duration / 1000)
                    : 0;

                meterRegistry.gauge("batch.step.throughput",
                    Tags.of("step", stepName), throughput);
            }
        }
    }

    private long getStepDuration(StepExecution stepExecution) {
        if (stepExecution.getEndTime() == null) {
            return System.currentTimeMillis() -
                stepExecution.getStartTime().getTime();
        }
        return stepExecution.getEndTime().getTime() -
            stepExecution.getStartTime().getTime();
    }
}
```

## 12.9 Scaling Best Practices

### Thread Configuration
```java
@Configuration
public class ThreadConfig {

    @Bean
    @Primary
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler =
            new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("Batch-Scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }

    @Bean("batchExecutor")
    public TaskExecutor batchExecutor() {
        int processors = Runtime.getRuntime()
            .availableProcessors();
        int poolSize = Math.max(4, processors * 2);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }
}
```

### Chunk Size Optimization
```java
@Component
public class DynamicChunkSizeStrategy {

    public int calculateChunkSize(long totalRecords) {
        if (totalRecords < 1000) {
            return 10;
        } else if (totalRecords < 100000) {
            return 100;
        } else if (totalRecords < 1000000) {
            return 500;
        } else {
            return 1000;
        }
    }

    public int calculateChunkSize(ExecutionContext context) {
        Long availableMemory = getAvailableMemory();
        Long estimatedItemSize = context.getLong("item.size", 100L);

        return (int) (availableMemory / estimatedItemSize / 10);
    }

    private long getAvailableMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
    }
}
```

## 12.10 Advanced Patterns

### Composite Job
```java
@Configuration
public class CompositeJobConfig {

    @Bean
    public Job compositeJob(JobRepository jobRepository,
                           Flow extractFlow,
                           Flow transformFlow,
                           Flow loadFlow) {

        Flow mainFlow = new FlowBuilder<>("mainFlow")
            .start(extractFlow)
            .next(transformFlow)
            .next(loadFlow)
            .build();

        return new JobBuilder("compositeJob", jobRepository)
            .start(mainFlow)
            .end()
            .build();
    }

    @Bean
    public Flow extractFlow(Step extractStep) {
        return new FlowBuilder<>("extractFlow")
            .start(extractStep)
            .build();
    }

    @Bean
    public Flow transformFlow(Step transformStep) {
        return new FlowBuilder<>("transformFlow")
            .start(transformStep)
            .split(threadPoolTaskExecutor())
            .add(createParallelFlow(transformStep))
            .build();
    }

    @Bean
    public Flow loadFlow(Step loadStep) {
        return new FlowBuilder<>("loadFlow")
            .start(loadStep)
            .build();
    }
}
```

### Conditional Parallel Processing
```java
@Configuration
public class ConditionalParallelConfig {

    @Bean
    public Flow conditionalFlow(Step stepA,
                                Step stepB,
                                Step aggregateStep,
                                TaskExecutor taskExecutor) {

        Flow parallelFlow = new FlowBuilder<>("parallelFlow")
            .start(stepA)
            .split(taskExecutor)
            .add(new FlowBuilder<>("parallelB")
                .start(stepB)
                .build())
            .build();

        return new FlowBuilder<>("conditionalFlow")
            .start(parallelFlow)
            .next(aggregateStep)
            .build();
    }
}
```

## 12.11 Practice Scenario

### Scenario: Large-scale Data Processing System
```java
@Configuration
public class LargeScaleProcessingConfig {

    @Bean
    public Job largeScaleProcessingJob(JobRepository jobRepository,
                                       Step partitionStep) {
        return new JobBuilder("largeScaleProcessingJob", jobRepository)
            .start(partitionStep)
            .build();
    }

    @Bean
    public Step partitionStep(JobRepository jobRepository,
                             PartitionHandler partitionHandler,
                             Partitioner partitioner) {

        return new StepBuilder("partitionStep", jobRepository)
            .partitioner("workerStep", partitioner)
            .partitionHandler(partitionHandler)
            .build();
    }

    @Bean
    public PartitionHandler partitionHandler(
            @Qualifier("processingExecutor") TaskExecutor executor,
            Step workerStep) {

        TaskExecutorPartitionHandler handler =
            new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(executor);
        handler.setStep(workerStep);
        handler.setGridSize(8);  // 8 partitions
        return handler;
    }

    @Bean("processingExecutor")
    public TaskExecutor processingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Processing-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);
        executor.initialize();
        return executor;
    }

    @Bean
    public Partitioner dataPartitioner() {
        return new ColumnRangePartitioner(
            "transactions",
            "transaction_date",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31));
    }

    @Bean
    public Step workerStep(JobRepository jobRepository,
                          PlatformTransactionManager txManager,
                          @Qualifier("partitionReader") ItemReader<Transaction> reader,
                          ItemProcessor<Transaction, ProcessedTransaction> processor,
                          ItemWriter<ProcessedTransaction> writer) {

        return new StepBuilder("workerStep", jobRepository)
            .<Transaction, ProcessedTransaction>chunk(500, txManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skip(Exception.class)
            .skipLimit(100)
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<Transaction> partitionReader(
            @Value("#{stepExecutionContext[startDate]}") String startDate,
            @Value("#{stepExecutionContext[endDate]}") String endDate,
            DataSource dataSource) {

        String sql = "SELECT * FROM transactions " +
            "WHERE transaction_date BETWEEN ? AND ? " +
            "ORDER BY transaction_date";

        return new JdbcCursorItemReaderBuilder<Transaction>()
            .dataSource(dataSource)
            .sql(sql)
            .rowMapper(new BeanPropertyRowMapper<>(Transaction.class))
            .preparedStatementSetter(
                new ArgumentPreparedStatementSetter(
                    new Object[]{
                        LocalDate.parse(startDate),
                        LocalDate.parse(endDate)
                    }))
            .build();
    }
}
```

## 12.12 Summary

| Feature | Purpose | Use Case |
|---------|---------|----------|
| Partitioning | Divide work across workers | Large datasets |
| Parallel Flow | Execute flows in parallel | Independent steps |
| Remote Chunking | Distributed processing | Scale beyond single JVM |
| Multi-threaded Step | Thread-per-chunk | High throughput |
| Throttle Limit | Control concurrency | Resource management |

## Completion Checklist

- [x] Chapter 1: Quick Start - Setup and first batch job
- [x] Chapter 2: Core Concepts - Job, Step, JobRepository
- [x] Chapter 3: Job Configuration - Flows, decisions, listeners
- [x] Chapter 4: Step Types - Tasklet, Chunk-oriented
- [x] Chapter 5: Item Readers - JDBC, MongoDB, File readers
- [x] Chapter 6: Item Writers - Database, File, Message writers
- [x] Chapter 7: Item Processors - Transformation, validation
- [x] Chapter 8: Listeners - Job, Step, Chunk listeners
- [x] Chapter 9: Testing Strategies - Unit and integration tests
- [x] Chapter 10: Scheduling - Cron jobs, Quartz
- [x] Chapter 11: Error Handling - Retry, skip, restart
- [x] Chapter 12: Advanced Features - Partitioning, parallel processing

## Next Steps

### Practice Projects
1. **ETL Pipeline**: Build a complete ETL job with validation
2. **Report Generator**: Scheduled report generation system
3. **Data Migration**: Multi-source data migration job
4. **Real-time Processing**: Stream-based batch processing

### Advanced Topics to Explore
- Spring Cloud Data Flow
- Spring Batch Admin (deprecated, use Spring Boot)
- Custom ItemReaders/Writers
- Spring Batch with Kubernetes

### Resources
- [Official Documentation](https://docs.spring.io/spring-batch/)
- [Spring Batch in Action](https://www.manning.com/books/spring-batch-in-action)
- [Spring Batch GitHub](https://github.com/spring-projects/spring-batch)

---

**Congratulations! You've completed the Spring Batch Learning Guide.**

Total Learning Time: **15-17 hours**

---
*Duration: 2 hours*
