# Chapter 14: 实战分析 — 多文件并发 CPU 高问题排查与优化

## Overview

本章分析一个真实生产场景：Spring Batch 服务通过 API 接收 14 种文件同时处理，CPU 飙升到 95%+ 的根因排查和优化方案。**约束：不能修改 API 触发 Job 的方式**，所有优化必须在 Job/Step 配置内部完成。

```
┌────────────┐     HTTP API x14      ┌──────────────────────┐
│   客户端    │ ──────────────────────→ │  Spring Batch 服务     │
│  14个文件   │  异步启动 Job          │                      │
│  每种 1G+  │                        │  JobLauncher          │
│  | 分隔     │                        │  .runAsync()         │
└────────────┘                        └──────────┬───────────┘
                                                 │
                    ┌────────────────────────────┼────────────┐
                    │                            │            │
               ┌────▼────┐               ┌──────▼──────┐    ...
               │ Job-1   │               │  Job-14     │
               │ Step    │               │  Step       │
               │ throttle=2              │  throttle=2 │
               │ SimpleAsyncTaskExecutor  │  SimpleAsyncTaskExecutor
               └─────────┘               └─────────────┘
                           │              │
                           ▼              ▼
                    ┌──────────────────────────┐
                    │    8 CPU Cores            │
                    │    50-80 线程竞争          │
                    │    CPU 95%+               │
                    └──────────────────────────┘
```

---

## 14.1 场景描述

### 14.1.1 系统架构

```
服务架构:
├── Spring Boot + Spring Batch 服务
├── API 接口: POST /api/file/process?fileType=X&filePath=...
├── 客户端: 14 种文件同时通过 API 发送
├── 每种文件: 1G+, ~100 万条记录, | 分隔
├── 服务端: 异步启动 Job（JobLauncher.runAsync）
│
├── Job 配置（每个文件一个 Job 实例）:
│   ├── FlatFileItemReader (+ SynchronizedItemStreamReader 包装)
│   ├── ItemProcessor（每种类型解析逻辑不同）
│   ├── JdbcBatchItemWriter（写入不同表）
│   ├── throttleLimit = 2
│   └── SimpleAsyncTaskExecutor ← ★ 关键问题
│
└── 服务规格: 8 CPU 核
```

### 14.1.2 问题现象

```
CPU 使用率: 95%+（持续波动）
平均负载: 远高于 CPU 核数
GC 频率: Young GC 每 3-5 秒，Full GC 每 30-60 秒
API 响应: 超时增加（虽然 Job 是异步的，但 API 线程也被影响）
文件处理: 所有文件都能处理完，但系统整体抖动严重
```

### 14.1.3 优化约束

```
约束条件:
┌────────────────────────────────────────────┐
│ 1. API 层不能改动                            │
│    - Controller 代码不变                    │
│    - 不能加 Semaphore 限流                  │
│    - 14 个 Job 仍然同时异步启动              │
│                                            │
│ 2. JobLauncher 不能改动                     │
│    - 不能改异步启动方式                      │
│    - 不能加排队机制                          │
│                                            │
│ 3. 只能改 Job/Step 配置内部                  │
│    - TaskExecutor 替换                     │
│    - Step 参数调整                          │
│    - Bean 共享化                           │
└────────────────────────────────────────────┘
```

---

## 14.2 根因排查

### 14.2.1 `SimpleAsyncTaskExecutor` 源码分析

`SimpleAsyncTaskExecutor` 是罪魁祸首。它的问题是**每次 `execute()` 都创建一个新线程，没有上限、没有复用**。

```java
// Spring 5.x SimpleAsyncTaskExecutor 源码（简化）
public class SimpleAsyncTaskExecutor implements TaskExecutor {

    private int concurrencyLimit = -1;  // -1 = 无限制!

    @Override
    public void execute(Runnable task) {
        // ★ 每次调用都 new Thread()，无复用！
        Thread thread = new Thread(task, "SimpleAsync-" + counter++);
        thread.start();
        // ★ 线程执行完毕后被 OS 回收
        // ★ 下次 execute 又创建新线程
    }
}
```

**问题链**：

```
SimpleAsyncTaskExecutor.execute(chunkTask)
  → new Thread()           ← 分配 ~1MB 栈内存
  → thread.start()         ← OS 创建内核线程
  → 线程调度到 CPU          ← 参与上下文切换竞争
  → 执行 chunk（读→解析→写）
  → 线程结束                ← 线程对象变为可回收
  → 下一个 chunk 又 new Thread()  ← 重复整个过程
```

### 14.2.2 线程数失控计算

```
14 个 Job 同时运行，每个 Step throttleLimit=2

静态计算:
  14 Jobs × 2 chunk 线程 = 28 线程（这是最起码的）
  + 14 个 JobLauncher 异步线程 = 14 线程
  + 框架内部线程（事务同步、监听器、JobExplorer 等）≈ 8-16 线程
  = 50-58 线程

动态波动:
  SimpleAsyncTaskExecutor 没有线程池复用，
  每个 chunk 完成 → 旧线程销毁 → 新 chunk 创建新线程
  线程数量在 40-80 之间持续波动

超卖比:
  50-80 线程 / 8 CPU 核 = 6.25x - 10x
  → OS 花 30%+ 的时间在上下文切换上
```

### 14.2.3 CPU 时间分布

```
实际测量（估计）:
┌──────────────────────────────────────────────┐
│ 实际业务（解析+过滤+写入）:    10-15%           │
│ OS 上下文切换:                 30-35%  ← 最大  │
│ 线程创建/销毁:                 15-20%          │
│ synchronized 锁自旋:           15-20%          │
│ GC 暂停:                      10-15%           │
│ 其他（框架调度、日志等）:       5-10%           │
└──────────────────────────────────────────────┘

结论: 85%+ 的 CPU 没有在干"正事"！
```

### 14.2.4 根因汇总

| 根因 | 贡献度 | 说明 |
|------|--------|------|
| `SimpleAsyncTaskExecutor` 无上限创建线程 | 35% | 50-80 线程争 8 核，是根本问题 |
| 上下文切换 | 30% | 10x 超卖比导致 OS 频繁切换 |
| 线程创建/销毁开销 | 15% | 每次 new Thread + 1MB 栈分配 |
| `synchronized` 锁自旋 | 10% | Reader 上多线程空等 |
| GC 压力 | 10% | 短生命周期 String 对象暴增 |

---

## 14.3 核心方案：共享 ThreadPoolTaskExecutor

### 14.3.1 思路

不改 API、不改 JobLauncher，只改一行配置：**所有 Step 共享同一个 `ThreadPoolTaskExecutor` Bean**。

之前是每个 Step 有自己的 `SimpleAsyncTaskExecutor`，现在是所有 Step 引用同一个 `ThreadPoolTaskExecutor`，用线程池的有界特性自然限流。

```
优化前: 每个 Step 独立 SimpleAsyncTaskExecutor
  Job-1 → SimpleAsync-1 → new Thread() → new Thread() → ...（无上限）
  Job-2 → SimpleAsync-2 → new Thread() → new Thread() → ...（无上限）
  ...
  Job-14→ SimpleAsync-14→ new Thread() → new Thread() → ...（无上限）
  ─────────────────────────────────────────────────────────
  总线程: 无上限，50-80+ 线程

优化后: 所有 Step 共享 ThreadPoolTaskExecutor
  Job-1 ─┐
  Job-2 ─┼──→ SharedThreadPool(core=4, max=4, queue=∞)
  ...    │    ┌──────────────────┐
  Job-14─┘    │ ● ● ● ● ○ ○ ○ ○│ ← 最多 4 个活跃线程
              │ 队列自动排队      │
              └──────────────────┘
  ─────────────────────────────────────────────────────────
  总线程: 4（刚好匹配 CPU 核数）
```

### 14.3.2 实现代码

**改动点 1：定义共享线程池 Bean**（一次定义，到处引用）

```java
@Configuration
public class SharedTaskExecutorConfig {

    // ========== 所有 Step 共享的线程池 ==========
    @Bean("sharedBatchTaskExecutor")
    public TaskExecutor sharedBatchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // ★ 核心: corePoolSize = CPU 核数 / 2
        //   太多 → 上下文切换; 太少 → CPU 空闲
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(Integer.MAX_VALUE);  // 无限队列，不拒绝
        executor.setThreadNamePrefix("batch-chunk-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
```

**改动点 2：Step 引用共享线程池**（唯一需要的改动的就是这里）

```java
@Configuration
public class FileJobConfig {

    @Bean
    @StepScope
    public Step fileStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager,
                         @Value("#{jobParameters['fileType']}") String fileType,
                         @Value("#{jobParameters['filePath']}") String filePath,
                         DataSource dataSource,
                         @Qualifier("sharedBatchTaskExecutor") TaskExecutor sharedExecutor) {

        return new StepBuilder("fileStep", jobRepository)
            .<RawRecord, ParsedRecord>chunk(2000, transactionManager)
            .reader(createReader(filePath))
            .processor(createProcessor(fileType))
            .writer(createWriter(fileType, dataSource))
            // ★ 唯一改动: 从 SimpleAsyncTaskExecutor 改为共享线程池
            .taskExecutor(sharedExecutor)
            .throttleLimit(2)     // 单个 Job 最多 2 个 chunk 并行
            .build();
    }
}
```

### 14.3.3 工作原理

```
14 个 Job 同时运行，共享线程池 corePoolSize=4:

时间轴:

T0: Job-1 启动, 提交 chunk-1 → 线程池创建 Thread-1 执行
    Job-1 提交 chunk-2 → 线程池创建 Thread-2 执行
    Job-2 提交 chunk-1 → 线程池创建 Thread-3 执行
    Job-2 提交 chunk-2 → 线程池创建 Thread-4 执行
    Job-3 提交 chunk-1 → 线程池已满，进入队列等待
    Job-3 提交 chunk-2 → 进入队列等待
    ...

T1: Thread-1 (Job-1 chunk-1) 完成 → 从队列取出 Job-3 chunk-1 执行
    Thread-2 (Job-1 chunk-2) 完成 → 从队列取出 Job-3 chunk-2 执行
    ...

效果:
- 任何时候只有 4 个 chunk 线程活跃
- 线程复用（不用 new/destroy）
- 14 个 Job 的 chunk 自动排队
- CPU 平稳，无超卖
```

### 14.3.4 为什么 `throttleLimit=2` 仍然有效

```
throttleLimit=2 是"每个 Step 内部"的限制
corePoolSize=4 是"全局"的限制

两者结合的效果:
  Step-1 最多 2 个 chunk 并发  ← throttleLimit
  Step-2 最多 2 个 chunk 并发
  ...
  但全局只有 4 个线程可用  ← corePoolSize
  → 最多同时有 4 个 chunk 在运行
  → 多余的 chunk 在线程池排队

相当于: throttleLimit 是"软限制"，线程池大小是"硬限制"
```

### 14.3.5 收益量化

| 指标 | SimpleAsyncTaskExecutor | 共享 ThreadPoolTaskExecutor |
|------|------------------------|---------------------------|
| 活跃线程数 | 50-80 | 4-8 |
| CPU 使用率 | 95%+ | 60-70% |
| 上下文切换/秒 | 数十万次 | < 5000 次 |
| 线程创建开销 | 每 chunk 一次（无复用） | 仅首次创建（复用） |
| 14 文件总耗时 | 2-3 分钟 | 5-8 分钟 |
| 系统稳定性 | ❌ 抖动 | ✅ 平稳 |

> 总耗时从 2-3 分钟增加到 5-8 分钟，但这是**正常的、可预期的**——之前是用 10x 超卖的线程换来的虚假加速，代价是系统不稳定。优化后线程数 = CPU 核数，每个线程都在干正事，不再空转。

---

## 14.4 补充优化：连接池和 Chunk 调优

### 14.4.1 数据库连接池

```properties
# 当前: 10-20 个连接
# 问题: 4 个 chunk 线程 + 14 个 Job 共享 → 连接竞争

# 优化后:
spring.datasource.hikari.maximum-pool-size=12
# 公式: (共享线程池大小 × 2) + 预留
#      = (4 × 2) + 4 = 12
```

### 14.4.2 Chunk 大小

```java
// 文件→DB 场景，chunk 大小应偏大
// 原因: 文件 I/O 快，DB 批量写要凑够批数

// 当前: 500（偏小，事务频繁提交）
// 优化: 2000-5000

return new StepBuilder("fileStep", jobRepository)
    .<RawRecord, ParsedRecord>chunk(2000, transactionManager)  // ← 调大
    // ...
    .build();
```

### 14.4.3 JVM 参数

```bash
# 减少 GC 压力（当前大概率是默认的 -Xmx1g 或更小）
java -Xms4g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -jar app.jar
```

---

## 14.5 更深层优化：单线程 Step

如果 5-8 分钟的总耗时可以接受，最好的做法是**去掉 Step 的多线程配置**。因为文件读取是顺序 I/O，多线程在这个场景零收益。

```java
@Bean
@StepScope
public Step fileStep(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     @Value("#{jobParameters['fileType']}") String fileType,
                     @Value("#{jobParameters['filePath']}") String filePath,
                     DataSource dataSource) {

    return new StepBuilder("fileStep", jobRepository)
        .<RawRecord, ParsedRecord>chunk(2000, transactionManager)
        .reader(createReader(filePath))
        .processor(createProcessor(fileType))
        .writer(createWriter(fileType, dataSource))
        // ★ 不调 taskExecutor() → 单线程 Step
        // ★ 不调 throttleLimit() → 默认串行
        .build();
}
```

```
效果对比:
┌─────────────────────┬──────────────┬──────────────┬──────────────┐
│ 指标                 │ SimpleAsync  │ 共享线程池    │ 单线程 Step  │
├─────────────────────┼──────────────┼──────────────┼──────────────┤
│ 活跃线程数            │ 50-80       │ 4-8          │ 14（每个Job1个）│
│ CPU 使用率            │ 95%+        │ 60-70%       │ 40-50%       │
│ 14 文件总耗时          │ 2-3 min     │ 5-8 min      │ 10-15 min    │
│ 代码改动              │ -           │ 小            │ 小            │
│ 稳定性                │ ❌          │ ✅            │ ✅✅          │
│ 推荐度                │ ❌          │ ⭐⭐⭐⭐       │ ⭐⭐⭐⭐⭐      │
└─────────────────────┴──────────────┴──────────────┴──────────────┘
```

---

## 14.6 实操改动清单

### 最小改动方案（共享线程池）

```
改动文件: Step 配置类
改动行数: 2-3 行

变化:
- 新增 1 个 @Bean: sharedBatchTaskExecutor
- 修改 Step 方法参数: 注入 sharedBatchTaskExecutor
- 删除: SimpleAsyncTaskExecutor 的 @Bean 定义

不改:
- Controller（API）
- JobLauncher
- Job 定义
- Processor/Reader/Writer
```

### 最佳实践方案（单线程 Step）

```
改动文件: Step 配置类
改动行数: 2-3 行

变化:
- Step 中删除 .taskExecutor() 调用
- Step 中删除 .throttleLimit() 调用
- 删除: SimpleAsyncTaskExecutor 的 @Bean 定义

不改:
- Controller（API）
- JobLauncher
- Job 定义
- Processor/Reader/Writer
```

---

## 14.7 无 Grafana 的监控方案

不能引入 Grafana 的情况下，使用 **Spring Boot Actuator + 自定义日志 + JDK 内置工具** 同样能清晰看到优化收益。关键是在**优化前后各采集一次数据**做对比。

### 14.7.1 方式一：Spring Boot Actuator（零依赖，开箱即用）

Spring Boot 自带 Actuator，暴露 REST 接口，用 `curl` 即可采集。

**配置**：

```properties
# application.properties
management.endpoints.web.exposure.include=metrics,threaddump,heapdump
management.endpoint.metrics.enabled=true
```

**核心指标采集命令**：

```bash
# ==== 1. JVM 线程数（最直接的指标！） ====
curl -s http://localhost:8080/actuator/metrics/jvm.threads.live
# 优化前: 50-80
# 优化后: 4-8

# ==== 2. 线程状态分布 ====
curl -s http://localhost:8080/actuator/metrics/jvm.threads.states
# 返回各状态线程数:
#   runnable: X     ← 活跃干活线程
#   blocked:  Y     ← 锁等待（优化后应为 0）
#   waiting:  Z     ← 空闲等待

# ==== 3. GC 频率和耗时 ====
curl -s http://localhost:8080/actuator/metrics/jvm.gc.pause
# 优化前: Young GC 每 3-5s, 总耗时占比 20%+
# 优化后: Young GC 每 15-30s, 总耗时占比 < 5%

# ==== 4. CPU 使用率 ====
curl -s http://localhost:8080/actuator/metrics/system.cpu.usage
# 优化前: 0.95+
# 优化后: 0.60-0.70

# ==== 5. 进程 CPU ====
curl -s http://localhost:8080/actuator/metrics/process.cpu.usage

# ==== 6. 活跃线程数（直接看线程名） ====
curl -s http://localhost:8080/actuator/threaddump | jq '.threads | length'
# 优化前: 50-80 线程
# 优化后: 4-8 线程

# ==== 7. DB 连接池 ====
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

**优化前后对比脚本**：

```bash
#!/bin/bash
# collect-metrics.sh — 采集关键指标做对比

BASE_URL="http://localhost:8080/actuator"
OUTPUT="metrics-before.txt"  # 改为 metrics-after.txt 做优化后采集

echo "=== Timestamp: $(date) ===" > $OUTPUT
echo "" >> $OUTPUT

echo "--- Threads ---" >> $OUTPUT
curl -s "$BASE_URL/metrics/jvm.threads.live" >> $OUTPUT
echo "" >> $OUTPUT
curl -s "$BASE_URL/metrics/jvm.threads.states" >> $OUTPUT
echo "" >> $OUTPUT

echo "--- CPU ---" >> $OUTPUT
curl -s "$BASE_URL/metrics/system.cpu.usage" >> $OUTPUT
echo "" >> $OUTPUT
curl -s "$BASE_URL/metrics/process.cpu.usage" >> $OUTPUT
echo "" >> $OUTPUT

echo "--- GC ---" >> $OUTPUT
curl -s "$BASE_URL/metrics/jvm.gc.pause" >> $OUTPUT
echo "" >> $OUTPUT

echo "--- Thread Count ---" >> $OUTPUT
curl -s "$BASE_URL/threaddump" | jq '.threads | length' >> $OUTPUT
echo "" >> $OUTPUT

echo "=== Done ===" >> $OUTPUT
```

### 14.7.2 方式二：自定义 `StepExecutionListener` 日志（看每个文件的处理耗时）

加一个监听器，记录每个文件的处理耗时、行数、写入量。

```java
@Component
public class FileProcessingMetricsListener extends StepExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(
        FileProcessingMetricsListener.class);

    private long startTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.startTime = System.currentTimeMillis();

        String fileType = stepExecution.getJobParameters().getString("fileType");
        String filePath = stepExecution.getJobParameters().getString("filePath");

        log.info("===== 开始处理文件: type={}, path={} =====", fileType, filePath);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long duration = System.currentTimeMillis() - startTime;

        long readCount = stepExecution.getReadCount();
        long writeCount = stepExecution.getWriteCount();
        long skipCount = stepExecution.getSkipCount();
        long filterCount = readCount - writeCount - skipCount;
        long threadCount = Thread.activeCount();
        double cpuUsage = ManagementFactory.getOperatingSystemMXBean() instanceof
            com.sun.management.OperatingSystemMXBean osBean
            ? osBean.getProcessCpuLoad() : -1;

        // ★ 关键一行：包含所有需要对比的指标
        log.info(
            "===== 文件处理完成 | type={} | 耗时={}秒 | 读取={} | 写入={} | 过滤={} | 跳过={} | 活跃线程={} | CPU={}% =====",
            stepExecution.getJobParameters().getString("fileType"),
            duration / 1000,
            readCount,
            writeCount,
            filterCount,
            skipCount,
            threadCount,
            Math.round(cpuUsage * 100)
        );

        return exitStatus;
    }
}
```

**输出示例**：

```
优化前:
===== 文件处理完成 | type=A | 耗时=145秒 | 读取=1048576 | 写入=892134 | 过滤=156442 | 跳过=0 | 活跃线程=62 | CPU=95% =====

优化后（共享线程池）:
===== 文件处理完成 | type=A | 耗时=198秒 | 读取=1048576 | 写入=892134 | 过滤=156442 | 跳过=0 | 活跃线程=6 | CPU=65% =====

优化后（单线程 Step）:
===== 文件处理完成 | type=A | 耗时=245秒 | 读取=1048576 | 写入=892134 | 过滤=156442 | 跳过=0 | 活跃线程=1 | CPU=42% =====
```

**在 Step 上注册**：

```java
return new StepBuilder("fileStep", jobRepository)
    .<RawRecord, ParsedRecord>chunk(2000, transactionManager)
    .reader(reader)
    .processor(processor)
    .writer(writer)
    .listener(fileProcessingMetricsListener)  // ★ 注册监听器
    .build();
```

### 14.7.3 方式三：JDK 内置工具（零安装）

```bash
# ==== 1. jstack — 看线程数和线程状态 ====
#   优化前执行:
jstack -l <pid> > thread-dump-before.txt
#   优化后执行:
jstack -l <pid> > thread-dump-after.txt

#   对比线程数:
grep -c '"http' thread-dump-before.txt  # 优化前 http 线程数
grep -c '"SimpleAsync' thread-dump-before.txt  # SimpleAsync 线程数（优化前应有大量）
grep -c 'BLOCKED' thread-dump-before.txt  # 锁等待线程数

grep -c '"batch-chunk' thread-dump-after.txt  # 优化后 chunk 线程数
grep -c 'BLOCKED' thread-dump-after.txt  # 锁等待线程数（优化后应趋近 0）

# ==== 2. jstat — 看 GC 频率 ====
#   每 5 秒采样一次，看 GC 次数和耗时
jstat -gcutil <pid> 5000 10
#   S0  S1  E   O   M   YGC  YGCT  FGC  FGCT
#   优化前: E 区每 3-5 秒填满, YGC 频繁
#   优化后: E 区每 15-30 秒填满, FGC 消失

# ==== 3. jcmd — 一键查看线程数 ====
jcmd <pid> Thread.print | grep -c "tid="
# 优化前: 50-80
# 优化后: 4-8

# ==== 4. Java Flight Recorder (JFR) — 最详细的 profiling ====
#   录制 5 分钟:
jcmd <pid> JFR.start name=profile duration=5m filename=profile.jfr
#   导出后用 JDK Mission Control (JMC) 打开分析:
#   - Threads → 线程数、线程状态时间线
#   - CPU Load → CPU 使用率
#   - GC → GC 暂停时间
#   - Lock Instances → 锁竞争热点
```

### 14.7.4 方式四：操作系统级工具

```bash
# ==== 1. top（最直观） ====
top -H -p <pid>
#   优化前: 50-80 线程, CPU 95%+
#   优化后: 4-8 线程, CPU 60-70%

# ==== 2. pidstat — 上下文切换速率 ====
pidstat -w -p <pid> 5 10
#   cswch/s:  voluntary context switches（自愿切换）
#   nvcswch/s: non-voluntary context switches（强制切换，反映超卖）
#   优化前: nvcswch/s 数千到上万
#   优化后: nvcswch/s 两位数

# ==== 3. vmstat — 系统级 CPU 和上下文切换 ====
vmstat 5 10
#   procs  r  b   cpu us sy id wa st
#   优化前: r > 50（大量线程排队）, sy > 30%（系统 CPU 高）
#   优化后: r < 8（线程数正常）, sy < 10%

# ==== 4. /proc — 直接读内核统计 ====
cat /proc/<pid>/status | grep Threads
# 优化前: Threads: 50-80
# 优化后: Threads: 4-8
```

### 14.7.5 优化对比报告模板

将优化前后的数据填入即可看到清晰收益：

```
┌──────────────────────┬──────────────┬──────────────┬──────────────┐
│ 指标（来源）           │ 优化前        │ 优化后        │ 变化          │
├──────────────────────┼──────────────┼──────────────┼──────────────┤
│ 活跃线程数             │              │              │              │
│ (actuator/threaddump) │              │              │              │
├──────────────────────┼──────────────┼──────────────┼──────────────┤
│ CPU 使用率             │              │              │              │
│ (actuator/metrics)    │              │              │              │
├──────────────────────┼──────────────┼──────────────┼──────────────┤
│ 上下文切换/秒           │              │              │              │
│ (pidstat -w)          │              │              │              │
├──────────────────────┼──────────────┼──────────────┼──────────────┤
│ Young GC 间隔         │              │              │              │
│ (jstat -gcutil)       │              │              │              │
├──────────────────────┼──────────────┼──────────────┼──────────────┤
│ Full GC 次数          │              │              │              │
│ (jstat -gcutil)       │              │              │              │
├──────────────────────┼──────────────┼──────────────┼──────────────┤
│ 单个文件耗时(Listener) │              │              │              │
│ 14文件总耗时           │              │              │              │
└──────────────────────┴──────────────┴──────────────┴──────────────┘
```

**验证步骤**：

```
1. 优化前采集基线
   └── 14 个文件同时触发
   └── 运行 collect-metrics.sh 采集
   └── 运行 pidstat -w -p <pid> 5 10 采集上下文切换
   └── 观察 top 的 CPU 和线程数

2. 上线优化代码（共享 ThreadPoolTaskExecutor 或单线程 Step）

3. 优化后采集对比
   └── 同样 14 个文件同时触发
   └── 执行同样的采集命令
   └── 填入对比报告模板

4. 关键要看的数据差异
   └── 线程数下降: 50-80 → 4-8
   └── CPU 下降: 95% → 65%
   └── Blocked 线程消失: 从有到无
   └── Full GC 消失: 从频繁到 0
```

---

## 14.8 总结

```
核心结论:
┌────────────────────────────────────────────────────────────┐
│ 优化前（SimpleAsyncTaskExecutor）:                          │
│   50-80 线程 → CPU 95%+ → 3 分钟 → 系统抖动                │
│   其中 85% CPU 花在线程管理而不是业务逻辑                    │
│                                                            │
│ 优化后（共享 ThreadPoolTaskExecutor）:                      │
│   4 线程 → CPU 65% → 6 分钟 → 系统平稳                     │
│   每个线程都在干正事，没有浪费                              │
│                                                            │
│ 原则:                                                       │
│   线程数 = CPU 核数（或 CPU 核数/2）                        │
│   不要用 SimpleAsyncTaskExecutor                            │
│   文件 I/O 场景多线程 Step 没有意义                         │
│   全局限流比局部限流更有效                                  │
│   不改 API 层也能解决问题（改 Step 配置就够了）              │
└────────────────────────────────────────────────────────────┘
```

---

**扩展阅读**：
- 第4章 [Step Types](04-step-types.md) — Chunk-oriented Step 基础
- 第13章 [Multi-threaded Processing](13-multithreaded-processing.md) — 多线程 Step 原理
- [Spring Batch 官方文档 - Scaling](https://docs.spring.io/spring-batch/reference/scalability.html)
