# Chapter 2: Core Concepts

## Overview
Understand the fundamental building blocks of Spring Batch: Job, Step, JobRepository, and JobLauncher.

## 2.1 Spring Batch Architecture

### Layered Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                      Application Layer                       │
│                    (User Code - Jobs)                        │
├─────────────────────────────────────────────────────────────┤
│                     Batch Core Layer                         │
│         (Job, Step, JobLauncher, JobRepository)              │
├─────────────────────────────────────────────────────────────┤
│                     Infrastructure Layer                     │
│     (Readers, Writers, RetryTemplate, SkipPolicy)            │
└─────────────────────────────────────────────────────────────┘
```

## 2.2 Job Interface

### Job Definition
```java
public interface Job {
    String getName();
    void execute(JobExecution execution);
}
```

### Job Implementation with Multiple Steps
```java
@Configuration
public class MultiStepJobConfig {

    @Bean
    public Job multiStepJob(JobRepository jobRepository,
                            Step step1,
                            Step step2,
                            Step step3) {
        return new JobBuilder("multiStepJob", jobRepository)
            .start(step1)
            .next(step2)
            .next(step3)
            .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("extractStep", jobRepository)
            .tasklet(extractTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Step step2(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("transformStep", jobRepository)
            .tasklet(transformTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Step step3(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("loadStep", jobRepository)
            .tasklet(loadTasklet(), transactionManager)
            .build();
    }
}
```

### Flow-based Job
```java
@Bean
public Job flowJob(JobRepository jobRepository,
                   Flow splitFlow,
                   Step finalStep) {
    return new JobBuilder("flowJob", jobRepository)
        .start(splitFlow)
        .next(finalStep)
        .end()
        .build();
}

@Bean
public Flow splitFlow(Step stepA, Step stepB) {
    return new FlowBuilder<>("splitFlow")
        .start(stepA)
        .split(new SimpleAsyncTaskExecutor())
        .add(stepB)
        .build();
}
```

## 2.3 Step Interface

### Step Definition
```java
public interface Step {
    String getName();
    StepExecution getStepExecution(JobExecution jobExecution);
    void execute(JobExecution jobExecution) throws JobExecutionException;
}
```

### TaskletStep
```java
@Bean
public Step taskletStep(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager) {
    return new StepBuilder("taskletStep", jobRepository)
        .tasklet(myTasklet(), transactionManager)
        .allowStartIfComplete(true)  // Run even if completed
        .startLimit(3)               // Maximum 3 executions
        .build();
}
```

### Chunk-oriented Step
```java
@Bean
public Step chunkStep(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemReader<String> reader,
                      ItemProcessor<String, String> processor,
                      ItemWriter<String> writer) {
    return new StepBuilder("chunkStep", jobRepository)
        .<String, String>chunk(10, transactionManager)  // Chunk size = 10
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
}
```

## 2.4 JobRepository

### Purpose
- Persists job and step execution metadata
- Provides CRUD operations for job metadata
- Manages job execution state

### Configuration
```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager());
        factory.setTablePrefix("BATCH_");
        return factory.getObject();
    }
}
```

### Spring Boot Auto-configuration
Spring Boot auto-configures `JobRepository` when Spring Batch is on classpath:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:batchdb
  batch:
    jdbc:
      initialize-schema: always
      table-prefix: BATCH_
```

### Accessing JobRepository
```java
@Service
public class JobRepositoryService {

    private final JobRepository jobRepository;

    public List<JobInstance> getJobInstances(String jobName) {
        return jobRepository.findJobInstancesByJobName(jobName, 0, 100);
    }

    public List<JobExecution> getJobExecutions(JobInstance jobInstance) {
        return jobRepository.getJobExecutions(jobInstance);
    }

    public void update(JobExecution jobExecution) {
        jobRepository.update(jobExecution);
    }
}
```

## 2.5 JobLauncher

### Purpose
- Launches job execution with parameters
- Validates job parameters
- Returns JobExecution

### Configuration
```java
@Bean
public JobLauncher jobLauncher(JobRepository jobRepository) {
    SimpleJobLauncher launcher = new SimpleJobLauncher();
    launcher.setJobRepository(jobRepository);
    launcher.setTaskExecutor(new SyncTaskExecutor());  // Default: sync
    return launcher;
}
```

### Async Execution
```java
@Configuration
public class AsyncConfig {

    @Bean
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return launcher;
    }
}
```

### Launching Jobs
```java
@Service
public class JobLauncherService {

    private final JobLauncher jobLauncher;

    public JobExecution launchJob(String jobName, Map<String, Job> jobs,
                                  JobParameters params) throws JobExecutionException {
        Job job = jobs.get(jobName);
        if (job == null) {
            throw new IllegalArgumentException("Job not found: " + jobName);
        }
        return jobLauncher.run(job, params);
    }

    public CompletableFuture<JobExecution> launchAsync(Job job,
                                                       JobParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return jobLauncher.run(job, params);
            } catch (JobExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
```

## 2.6 JobParameters

### Creating JobParameters
```java
// Using JobParametersBuilder
JobParameters params = new JobParametersBuilder()
    .addString("input.file", "data.csv")
    .addLong("timestamp", System.currentTimeMillis())
    .addDouble("threshold", 0.5)
    .addDate("run.date", new Date())
    .toJobParameters();

// Using constructor
JobParameters params = new JobParameters(
    Map.of(
        "input.file", new JobParameter<>("data.csv"),
        "count", new JobParameter<>(5L),
        "date", new JobParameter<>(new Date(), Date.class)
    )
);
```

### Accessing in Tasklet
```java
Tasklet parametersTasklet = (contribution, chunkContext) -> {
    StepContext stepContext = chunkContext.getStepContext();
    JobParameters jobParameters = stepContext.getJobParameters();

    String filename = jobParameters.getString("input.file");
    Long count = jobParameters.getLong("count");
    Double threshold = jobParameters.getDouble("threshold");
    Date runDate = jobParameters.getDate("run.date");

    System.out.println("Filename: " + filename);
    System.out.println("Count: " + count);
    System.out.println("Threshold: " + threshold);
    System.out.println("Run Date: " + runDate);

    return RepeatStatus.FINISHED;
};
```

### Accessing in ItemProcessor
```java
public class ParameterAwareProcessor implements ItemProcessor<String, String> {

    @Override
    public String process(String item) throws Exception {
        ExecutionContext stepContext = context;
        JobParameters jobParameters = stepContext.getJobParameters();

        String mode = jobParameters.getString("mode", "default");
        // Use mode in processing
        return item.toUpperCase() + " [" + mode + "]";
    }
}
```

## 2.7 Metadata Tables

### Understanding the Schema

**BATCH_JOB_INSTANCE**
```sql
CREATE TABLE BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID BIGINT PRIMARY KEY,
    VERSION BIGINT,
    JOB_NAME VARCHAR(100) NOT NULL,
    JOB_KEY VARCHAR(2500) NOT NULL
);
```

**BATCH_JOB_EXECUTION**
```sql
CREATE TABLE BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID BIGINT PRIMARY KEY,
    VERSION BIGINT,
    JOB_INSTANCE_ID BIGINT NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP,
    END_TIME TIMESTAMP,
    STATUS VARCHAR(10),
    EXIT_CODE VARCHAR(2500),
    EXIT_MESSAGE VARCHAR(2500),
    LAST_UPDATED TIMESTAMP
);
```

**BATCH_STEP_EXECUTION**
```sql
CREATE TABLE BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID BIGINT PRIMARY KEY,
    VERSION BIGINT NOT NULL,
    STEP_NAME VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    START_TIME TIMESTAMP NOT NULL,
    END_TIME TIMESTAMP,
    STATUS VARCHAR(10),
    COMMIT_COUNT BIGINT,
    READ_COUNT BIGINT,
    FILTER_COUNT BIGINT,
    WRITE_COUNT BIGINT,
    EXIT_CODE VARCHAR(2500),
    EXIT_MESSAGE VARCHAR(2500)
);
```

### Querying Metadata
```java
@Service
public class BatchMetadataService {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getRecentJobExecutions() {
        return jdbcTemplate.queryForList(
            "SELECT * FROM BATCH_JOB_EXECUTION " +
            "ORDER BY CREATE_TIME DESC LIMIT 10");
    }

    public Map<String, Object> getJobExecutionDetails(Long executionId) {
        String sql = """
            SELECT je.*, ji.JOB_NAME
            FROM BATCH_JOB_EXECUTION je
            JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID
            WHERE je.JOB_EXECUTION_ID = ?
            """;
        return jdbcTemplate.queryForMap(sql, executionId);
    }

    public List<Map<String, Object>> getStepExecutions(Long jobExecutionId) {
        return jdbcTemplate.queryForList(
            "SELECT * FROM BATCH_STEP_EXECUTION " +
            "WHERE JOB_EXECUTION_ID = ? " +
            "ORDER BY START_TIME",
            jobExecutionId);
    }
}
```

## 2.8 JobExecution and StepExecution

### JobExecution
```java
public class JobExecution {
    private final JobParameters jobParameters;
    private final JobInstance jobInstance;
    private volatile ExecutionContext executionContext;
    private volatile BatchStatus status;
    private volatile Date startTime;
    private volatile Date endTime;
    private volatile String exitStatus;
    private volatile List<Throwable> failureExceptions;

    // Getters and methods
    public void upgradeStatus(BatchStatus status) { ... }
    public void addFailureException(Throwable t) { ... }
    public boolean isRunning() { ... }
    public boolean isUnsuccessful() { ... }
}
```

### StepExecution
```java
public class StepExecution {
    private final String stepName;
    private final JobExecution jobExecution;
    private volatile BatchStatus status;
    private volatile int readCount;
    private volatile int writeCount;
    private volatile int commitCount;
    private volatile int rollbackCount;
    private volatile int filterCount;
    private volatile ExecutionContext executionContext;

    // Methods
    public void incrementReadCount() { ... }
    public void incrementWriteCount(int count) { ... }
    public void incrementFilterCount(int count) { ... }
}
```

### Accessing in Listeners
```java
@JobScope
@Component
public class ExecutionStatsListener {

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        System.out.println("Step: " + stepExecution.getStepName());
        System.out.println("Status: " + stepExecution.getStatus());
        System.out.println("Read: " + stepExecution.getReadCount());
        System.out.println("Write: " + stepExecution.getWriteCount());
        System.out.println("Filter: " + stepExecution.getFilterCount());
        System.out.println("Commit: " + stepExecution.getCommitCount());
        System.out.println("Rollback: " + stepExecution.getRollbackCount());
    }
}
```

## 2.9 ExecutionContext

### Purpose
- Persists state across executions
- Shared between steps in a job
- Survives job restarts

### Usage in Tasklet
```java
Tasklet contextTasklet = (contribution, chunkContext) -> {
    ExecutionContext jobContext = chunkContext.getStepContext()
        .getJobExecutionContext();
    ExecutionContext stepContext = chunkContext.getStepContext()
        .getStepExecution().getExecutionContext();

    // Store in job context (shared across steps)
    jobContext.put("totalRecords", 1000);

    // Store in step context
    stepContext.put("processedRecords", 0);

    // Retrieve
    Integer total = jobContext.getInt("totalRecords");
    Integer processed = stepContext.getInt("processedRecords");

    return RepeatStatus.FINISHED;
};
```

### Persisting State
```java
public class StatefulTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {
        ExecutionContext ec = chunkContext.getStepContext()
            .getStepExecution().getExecutionContext();

        // Get previous count
        int count = ec.getInt("currentCount", 0);

        // Increment
        count++;
        ec.putInt("currentCount", count);

        System.out.println("Current count: " + count);

        // On restart, count will be restored
        return RepeatStatus.FINISHED;
    }
}
```

## 2.10 Practice Scenario

### Scenario: ETL Pipeline Job
Create an ETL (Extract, Transform, Load) job:

```java
@Configuration
public class EtlJobConfig {

    @Bean
    public Job etlJob(JobRepository jobRepository,
                      Step extractStep,
                      Step transformStep,
                      Step loadStep) {
        return new JobBuilder("etlJob", jobRepository)
            .start(extractStep)
            .next(transformStep)
            .next(loadStep)
            .build();
    }

    @Bean
    public Step extractStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager) {
        return new StepBuilder("extractStep", jobRepository)
            .tasklet(extractTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Step transformStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder("transformStep", jobRepository)
            .tasklet(transformTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Step loadStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager) {
        return new StepBuilder("loadStep", jobRepository)
            .tasklet(loadTasklet(), transactionManager)
            .build();
    }
}

@Component
class ExtractTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {
        ExecutionContext ec = chunkContext.getStepContext()
            .getStepExecution().getExecutionContext();

        System.out.println("EXTRACT: Reading data from source...");
        List<String> data = Arrays.asList("record1", "record2", "record3");
        ec.put("extractedData", data);
        ec.putInt("recordCount", data.size());

        return RepeatStatus.FINISHED;
    }
}

@Component
class TransformTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {
        ExecutionContext ec = chunkContext.getStepContext()
            .getStepExecution().getExecutionContext();

        @SuppressWarnings("unchecked")
        List<String> data = (List<String>) ec.get("extractedData");

        System.out.println("TRANSFORM: Processing " + data.size() + " records...");
        List<String> transformed = data.stream()
            .map(d -> "TRANSFORMED_" + d)
            .collect(Collectors.toList());

        ec.put("transformedData", transformed);

        return RepeatStatus.FINISHED;
    }
}

@Component
class LoadTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {
        ExecutionContext ec = chunkContext.getStepContext()
            .getStepExecution().getExecutionContext();

        @SuppressWarnings("unchecked")
        List<String> data = (List<String>) ec.get("transformedData");

        System.out.println("LOAD: Writing " + data.size() + " records to destination...");
        data.forEach(d -> System.out.println("  -> " + d));

        return RepeatStatus.FINISHED;
    }
}
```

## 2.11 Summary

| Component | Purpose | Key Interface/Class |
|-----------|---------|---------------------|
| Job | Batch process container | `Job`, `JobBuilder` |
| Step | Single processing phase | `Step`, `StepBuilder` |
| Tasklet | Simple unit of work | `Tasklet` |
| JobRepository | Persists metadata | `JobRepository` |
| JobLauncher | Launches jobs | `JobLauncher` |
| JobParameters | Job input parameters | `JobParameters` |
| ExecutionContext | Runtime state | `ExecutionContext` |

## 2.12 Next Steps

- [Chapter 3: Job Configuration](03-job-configuration.md)
- Learn about job flows, decisions, and listeners
- Understand job inheritance and validation

## Exercises

### Exercise 1: Job with Conditional Flow
Create a job that:
1. Reads a parameter `environment`
2. If `environment=dev`, skip certain steps
3. If `environment=prod`, run all steps

### Exercise 2: ExecutionContext Usage
Create a job that:
1. Counts items in Step 1
2. Uses the count in Step 2
3. Persists the final result

### Exercise 3: Query Metadata
Write a service that:
1. Lists all job executions for today
2. Shows success/failure rate
3. Displays average execution time

---
*Duration: 1 hour*
