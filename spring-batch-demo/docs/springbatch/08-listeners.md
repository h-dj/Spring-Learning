# Chapter 8: Listeners

## Overview
Master job, step, chunk, and item listeners for monitoring and intercepting batch operations.

## 8.1 Listener Types

```
Listeners
├── JobListener (@BeforeJob, @AfterJob)
├── StepListener
│   ├── @BeforeStep, @AfterStep
│   ├── @BeforeChunk, @AfterChunk, @AfterChunkError
│   ├── @BeforeRead, @AfterRead, @OnReadError
│   ├── @BeforeProcess, @AfterProcess, @OnProcessError
│   └── @BeforeWrite, @AfterWrite, @OnWriteError
├── ItemReadListener
├── ItemProcessListener
└── ItemWriteListener
```

## 8.2 Job Listener

### Annotation-based
```java
@Component
@JobScope
public class JobExecutionListener {

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("========================================");
        System.out.println("JOB STARTED: " +
            jobExecution.getJobInstance().getJobName());
        System.out.println("Start Time: " + jobExecution.getStartTime());
        System.out.println("Parameters: " + jobExecution.getJobParameters());
        System.out.println("========================================");
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        System.out.println("========================================");
        System.out.println("JOB COMPLETED");
        System.out.println("Status: " + jobExecution.getStatus());
        System.out.println("End Time: " + jobExecution.getEndTime());
        System.out.println("Exit Code: " + jobExecution.getExitStatus().getExitCode());
        System.out.println("Duration: " +
            (jobExecution.getEndTime().getTime() -
             jobExecution.getStartTime().getTime()) + " ms");
        System.out.println("========================================");
    }
}
```

### Interface-based
```java
public class CustomJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Pre-job logic
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Post-job logic
    }
}

@Bean
public Job listenerJob(JobRepository jobRepository,
                       Step step1,
                       CustomJobListener jobListener) {
    return new JobBuilder("listenerJob", jobRepository)
        .listener(jobListener)
        .start(step1)
        .build();
}
```

### Collecting Statistics
```java
@Component
@JobScope
public class JobStatisticsListener {

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("jobName", jobExecution.getJobInstance().getJobName());
        stats.put("status", jobExecution.getStatus());
        stats.put("startTime", jobExecution.getStartTime());
        stats.put("endTime", jobExecution.getEndTime());
        stats.put("durationMs", jobExecution.getEndTime().getTime() -
            jobExecution.getStartTime().getTime());

        // Get step statistics
        for (StepExecution stepExecution :
                jobExecution.getStepExecutions()) {
            stats.put("step." + stepExecution.getStepName() + ".readCount",
                stepExecution.getReadCount());
            stats.put("step." + stepExecution.getStepName() + ".writeCount",
                stepExecution.getWriteCount());
            stats.put("step." + stepExecution.getStepName() + ".commitCount",
                stepExecution.getCommitCount());
            stats.put("step." + stepExecution.getStepName() + ".rollbackCount",
                stepExecution.getRollbackCount());
        }

        // Log or send to monitoring
        System.out.println("Job Statistics: " + stats);
    }
}
```

## 8.3 Step Listener

### Annotation-based
```java
@Component
@StepScope
public class StepExecutionListener {

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("--- STEP STARTED: " + stepExecution.getStepName());
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("--- STEP COMPLETED: " + stepExecution.getStatus());
        System.out.println("    Read: " + stepExecution.getReadCount());
        System.out.println("    Write: " + stepExecution.getWriteCount());
        System.out.println("    Filter: " + stepExecution.getFilterCount());
        System.out.println("    Commit: " + stepExecution.getCommitCount());
        System.out.println("    Rollback: " + stepExecution.getRollbackCount());

        return stepExecution.getExitStatus();
    }
}
```

### Interface-based
```java
public class CustomStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Before step logic
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // After step logic
        return stepExecution.getExitStatus();
    }
}
```

## 8.4 Chunk Listener

```java
@Component
public class ChunkListener {

    @BeforeChunk
    public void beforeChunk(ChunkContext context) {
        String stepName = context.getStepContext().getStepName();
        long startTime = System.currentTimeMillis();

        context.getStepContext().getStepExecution()
            .getExecutionContext()
            .putLong("chunk.start.time", startTime);

        System.out.println(">>> Starting chunk for step: " + stepName);
    }

    @AfterChunk
    public void afterChunk(ChunkContext context) {
        long startTime = context.getStepContext().getStepExecution()
            .getExecutionContext().getLong("chunk.start.time");
        long duration = System.currentTimeMillis() - startTime;

        int itemsProcessed = context.getStepContext()
            .getStepExecution().getWriteCount();

        System.out.println("<<< Chunk completed in " + duration + "ms");
        System.out.println("    Items processed: " + itemsProcessed);
    }

    @AfterChunkError
    public void afterChunkError(ChunkContext context) {
        Throwable exception = context.getException();
        System.out.println("!!! Chunk processing failed");
        System.out.println("    Error: " + exception.getMessage());

        if (exception != null) {
            exception.printStackTrace();
        }
    }
}
```

## 8.5 Item Read Listener

```java
@Component
public class ItemReadListener {

    @BeforeRead
    public void beforeRead() {
        System.out.println("Reading next item...");
    }

    @AfterRead
    public void afterRead(Object item) {
        System.out.println("    Read item: " + item);
    }

    @OnReadError
    public void onReadError(Exception ex) {
        System.out.println("!!! Error reading item: " + ex.getMessage());
    }
}
```

### Aggregating Read Statistics
```java
@Component
public class AggregatingReadListener {

    private final AtomicInteger totalRead = new AtomicInteger(0);

    @AfterRead
    public void afterRead(Object item) {
        totalRead.incrementAndGet();
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext()
            .put("total.items.read", totalRead.get());
        return stepExecution.getExitStatus();
    }
}
```

## 8.6 Item Process Listener

```java
@Component
public class ItemProcessListener {

    @BeforeProcess
    public void beforeProcess(Object item) {
        System.out.println("    Processing: " + item);
    }

    @AfterProcess
    public void afterProcess(Object item, Object result) {
        System.out.println("    Processed: " + item + " -> " + result);
    }

    @OnProcessError
    public void onProcessError(Object item, Exception ex) {
        System.out.println("!!! Error processing: " + item);
        System.out.println("    Error: " + ex.getMessage());
    }
}
```

### Tracking Processing Errors
```java
@Component
public class ErrorTrackingProcessListener {

    private final Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

    @OnProcessError
    public void onProcessError(Object item, Exception ex) {
        String errorType = ex.getClass().getSimpleName();
        errorCounts.merge(errorType, 1, Integer::sum);
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        System.out.println("Processing Errors:");
        errorCounts.forEach((error, count) ->
            System.out.println("    " + error + ": " + count));
    }
}
```

## 8.7 Item Write Listener

```java
@Component
public class ItemWriteListener {

    @BeforeWrite
    public void beforeWrite(List<?> items) {
        System.out.println(">>> Writing " + items.size() + " items...");
    }

    @AfterWrite
    public void afterWrite(List<?> items) {
        System.out.println("<<< Written " + items.size() + " items successfully");
    }

    @OnWriteError
    public void onWriteError(Exception ex, List<?> items) {
        System.out.println("!!! Error writing items: " + ex.getMessage());
        System.out.println("    Items affected: " + items.size());
    }
}
```

### Batching Statistics
```java
@Component
public class WriteStatisticsListener {

    private final AtomicLong totalWritten = new AtomicLong(0);

    @AfterWrite
    public void afterWrite(List<?> items) {
        totalWritten.addAndGet(items.size());
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        System.out.println("Total items written: " + totalWritten.get());
    }
}
```

## 8.8 Skip Listener

```java
@Component
public class SkipListener implements SkipListener<Object, Object> {

    private final Map<String, Integer> skipCounts = new ConcurrentHashMap<>();

    @Override
    public void onSkipInRead(Throwable t) {
        String errorType = t.getClass().getSimpleName();
        skipCounts.merge("read." + errorType, 1, Integer::sum);
        System.out.println("!!! Skipped item in read: " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        String errorType = t.getClass().getSimpleName();
        skipCounts.merge("process." + errorType, 1, Integer::sum);
        System.out.println("!!! Skipped item in process: " + item);
        System.out.println("    Error: " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        String errorType = t.getClass().getSimpleName();
        skipCounts.merge("write." + errorType, 1, Integer::sum);
        System.out.println("!!! Skipped item in write: " + item);
        System.out.println("    Error: " + t.getMessage());
    }

    public Map<String, Integer> getSkipCounts() {
        return skipCounts;
    }
}
```

## 8.9 Listener Configuration

### Using Builder
```java
@Bean
public Step configuredStep(JobRepository jobRepository,
                           PlatformTransactionManager txManager,
                           ItemReader<String> reader,
                           ItemWriter<String> writer) {

    return new StepBuilder("configuredStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .listener(new StepExecutionListener())
        .listener(new ItemReadListener())
        .listener(new ItemWriteListener())
        .build();
}
```

### Annotation Scanning
```java
@Configuration
@EnableBatchProcessing
@ComponentScan("cn.reid.springbatchdemo.listener")
public class ListenerConfig {
    // Listeners annotated with @Component will be auto-discovered
}
```

### Manual Registration
```java
@Bean
public Step manualListenerStep(JobRepository jobRepository,
                               PlatformTransactionManager txManager,
                               ItemReader<String> reader,
                               ItemWriter<String> writer) {

    StepBuilder stepBuilder = new StepBuilder("manualListenerStep", jobRepository);
    return stepBuilder
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .listener((StepExecutionListener) new CustomStepListener())
        .listener((ItemReadListener) new CustomReadListener())
        .listener((ItemWriteListener) new CustomWriteListener())
        .build();
}
```

## 8.10 Metrics and Monitoring

### Micrometer Integration
```java
@Component
public class BatchMetricsListener {

    private final MeterRegistry meterRegistry;

    public BatchMetricsListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @AfterJob
    public void recordJobMetrics(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        meterRegistry.counter("batch.job.completed",
            "job", jobName,
            "status", jobExecution.getStatus().toString()
        ).increment();

        long duration = jobExecution.getEndTime().getTime() -
            jobExecution.getStartTime().getTime();
        meterRegistry.timer("batch.job.duration",
            "job", jobName
        ).record(Duration.ofMillis(duration));

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            String stepName = stepExecution.getStepName();

            meterRegistry.counter("batch.step.read",
                "step", stepName
            ).increment(stepExecution.getReadCount());

            meterRegistry.counter("batch.step.write",
                "step", stepName
            ).increment(stepExecution.getWriteCount());
        }
    }
}
```

### Health Indicator
```java
@Component
public class BatchHealthIndicator
        extends HealthIndicator {

    @Autowired
    private JobRepository jobRepository;

    @Override
    protected Health health() {
        try {
            List<JobExecution> runningExecutions = getRunningExecutions();
            if (runningExecutions.isEmpty()) {
                return Health.up()
                    .withDetail("runningJobs", 0)
                    .build();
            }
            return Health.up()
                .withDetail("runningJobs", runningExecutions.size())
                .withDetail("jobs", runningExecutions.stream()
                    .map(je -> je.getJobInstance().getJobName())
                    .collect(Collectors.toList()))
                .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }

    private List<JobExecution> getRunningExecutions() {
        // Query job repository for running executions
        return Collections.emptyList();
    }
}
```

## 8.11 Logging Configuration

```java
@Configuration
public class BatchLoggingConfig {

    @Bean
    @JobScope
    public StepExecutionLoggingListener stepExecutionLoggingListener() {
        return new StepExecutionLoggingListener();
    }
}

public class StepExecutionLoggingListener
        implements StepExecutionListener {

    private static final Logger logger =
        LoggerFactory.getLogger(StepExecutionLoggingListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("Starting step: {} for job: {}",
            stepExecution.getStepName(),
            stepExecution.getJobExecution().getJobInstance().getJobName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("Completed step: {} with status: {}",
            stepExecution.getStepName(),
            stepExecution.getStatus());

        if (stepExecution.getStatus() == BatchStatus.FAILED) {
            logger.error("Step {} failed with {} rollbacks",
                stepExecution.getStepName(),
                stepExecution.getRollbackCount());
        }

        return stepExecution.getExitStatus();
    }
}
```

## 8.12 Practice Scenario

### Scenario: Comprehensive Monitoring System
```java
@Configuration
public class MonitoringJobConfig {

    @Bean
    public Job monitoringJob(JobRepository jobRepository,
                            Step step1,
                            JobExecutionListener jobListener,
                            StepExecutionListener stepListener) {
        return new JobBuilder("monitoringJob", jobRepository)
            .listener(jobListener)
            .start(step1)
            .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager txManager,
                      ItemReader<String> reader,
                      ItemWriter<String> writer,
                      ItemReadListener readListener,
                      ItemWriteListener writeListener) {

        return new StepBuilder("monitoredStep", jobRepository)
            .<String, String>chunk(10, txManager)
            .reader(reader)
            .writer(writer)
            .listener(stepListener)
            .listener(readListener)
            .listener(writeListener)
            .build();
    }
}

@Component
@JobScope
public class MonitoringJobListener {

    private final MeterRegistry meterRegistry;
    private final long startTime;

    public MonitoringJobListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.startTime = System.currentTimeMillis();
    }

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("========================================");
        System.out.println("JOB STARTED: " +
            jobExecution.getJobInstance().getJobName());
        System.out.println("Job Parameters: " +
            jobExecution.getJobParameters());
        System.out.println("========================================");
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("========================================");
        System.out.println("JOB COMPLETED");
        System.out.println("Status: " + jobExecution.getStatus());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("========================================");

        // Record metrics
        meterRegistry.timer("batch.job.duration",
            "job", jobExecution.getJobInstance().getJobName(),
            "status", jobExecution.getStatus().toString()
        ).record(Duration.ofMillis(duration));

        // Step statistics
        for (StepExecution stepExecution :
                jobExecution.getStepExecutions()) {
            System.out.println("\nStep: " + stepExecution.getStepName());
            System.out.println("  Read: " + stepExecution.getReadCount());
            System.out.println("  Write: " + stepExecution.getWriteCount());
            System.out.println("  Filter: " + stepExecution.getFilterCount());
            System.out.println("  Commit: " + stepExecution.getCommitCount());
            System.out.println("  Rollback: " + stepExecution.getRollbackCount());
        }
    }
}
```

## 8.13 Summary

| Listener | Methods | Purpose |
|----------|---------|---------|
| JobListener | @BeforeJob, @AfterJob | Job lifecycle |
| StepListener | @BeforeStep, @AfterStep | Step lifecycle |
| ChunkListener | @BeforeChunk, @AfterChunk, @AfterChunkError | Chunk processing |
| ItemReadListener | @BeforeRead, @AfterRead, @OnReadError | Item reading |
| ItemProcessListener | @BeforeProcess, @AfterProcess, @OnProcessError | Item processing |
| ItemWriteListener | @BeforeWrite, @AfterWrite, @OnWriteError | Item writing |
| SkipListener | onSkipInRead/Process/Write | Error handling |

## 8.14 Next Steps

- [Chapter 9: Testing Strategies](09-testing-strategies.md)
- Learn unit and integration testing
- Test jobs, steps, and listeners

## Exercises

### Exercise 1: Statistics Listener
Create a listener that:
1. Tracks read, write, and filter counts
2. Calculates processing rate
3. Logs summary at job end

### Exercise 2: Error Tracking
Create a skip listener that:
1. Captures all skipped items
2. Logs error details
3. Reports at job end

### Exercise 3: Performance Monitoring
Add Micrometer metrics to:
1. Record job duration
2. Track items processed per second
3. Monitor memory usage

---
*Duration: 1 hour*
