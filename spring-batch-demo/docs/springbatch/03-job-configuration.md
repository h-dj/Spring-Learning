# Chapter 3: Job Configuration

## Overview
Master job configuration including flows, decisions, listeners, and validation.

## 3.1 Job Flow Configuration

### Sequential Flow
```java
@Bean
public Job sequentialJob(JobRepository jobRepository,
                         Step step1,
                         Step step2,
                         Step step3) {
    return new JobBuilder("sequentialJob", jobRepository)
        .start(step1)
        .next(step2)
        .next(step3)
        .build();
}
```

### Conditional Flow with Transitions
```java
@Bean
public Job conditionalJob(JobRepository jobRepository,
                          Step processStep,
                          Step successStep,
                          Step failureStep) {
    return new JobBuilder("conditionalJob", jobRepository)
        .start(processStep)
        .on("COMPLETED").to(successStep)
        .on("FAILED").to(failureStep)
        .end()
        .build();
}

@Bean
public Step processStep(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager) {
    return new StepBuilder("processStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            boolean success = Math.random() > 0.5;
            if (success) {
                System.out.println("Processing successful!");
                return RepeatStatus.FINISHED;
            } else {
                System.out.println("Processing failed!");
                throw new RuntimeException("Processing failed!");
            }
        }, transactionManager)
        .build();
}
```

### Using ExitStatus for Transitions
```java
@Bean
public Step conditionalStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager) {
    return new StepBuilder("conditionalStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            String condition = chunkContext.getStepContext()
                .getJobParameters().getString("condition");

            if ("success".equals(condition)) {
                contribution.setExitStatus("COMPLETED");
            } else {
                contribution.setExitStatus("FAILED");
            }
            return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
}
```

## 3.2 JobDecider

### Custom Decider
```java
public class TimeBasedDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution,
                                      StepExecution stepExecution) {
        LocalTime now = LocalTime.now();

        if (now.isBefore(LocalTime.NOON)) {
            return new FlowExecutionStatus("MORNING");
        } else if (now.isBefore(LocalTime.of(18, 0))) {
            return new FlowExecutionStatus("AFTERNOON");
        } else {
            return new FlowExecutionStatus("EVENING");
        }
    }
}
```

### Using Decider in Job
```java
@Bean
public Job deciderJob(JobRepository jobRepository,
                      Step morningStep,
                      Step afternoonStep,
                      Step eveningStep,
                      TimeBasedDecider decider) {
    return new JobBuilder("deciderJob", jobRepository)
        .start(decider)
        .from(decider).on("MORNING").to(morningStep)
        .from(decider).on("AFTERNOON").to(afternoonStep)
        .from(decider).on("EVENING").to(eveningStep)
        .from(morningStep).on("*").to(decider)  // Loop back
        .from(afternoonStep).on("*").to(decider)
        .from(eveningStep).on("*").end()
        .build();
}
```

### Built-in Deciders
```java
@Bean
public Job fixedDeciderJob(JobRepository jobRepository,
                           Step step1,
                           Step step2,
                           Step step3) {
    return new JobBuilder("fixedDeciderJob", jobRepository)
        .start(step1)
        .on("COMPLETED_WITH_SKIPS").to(step2)
        .from(step1).on("*").to(step3)
        .end()
        .build();
}
```

## 3.3 Split Flows (Parallel Processing)

### Sequential Split
```java
@Bean
public Flow sequentialFlow(Step step1, Step step2) {
    return new FlowBuilder<>("sequentialFlow")
        .start(step1)
        .next(step2)
        .build();
}

@Bean
public Job splitFlowJob(JobRepository jobRepository,
                        Flow sequentialFlow,
                        Step finalStep) {
    return new JobBuilder("splitFlowJob", jobRepository)
        .start(sequentialFlow)
        .next(finalStep)
        .build();
}
```

### Parallel Split
```java
@Bean
public Flow flowA(Step stepA) {
    return new FlowBuilder<>("flowA")
        .start(stepA)
        .build();
}

@Bean
public Flow flowB(Step stepB) {
    return new FlowBuilder<>("flowB")
        .start(stepB)
        .build();
}

@Bean
public Job parallelJob(JobRepository jobRepository,
                       Flow flowA,
                       Flow flowB,
                       Step aggregateStep) {
    return new JobBuilder("parallelJob", jobRepository)
        .start(flowA)
        .split(new SimpleAsyncTaskExecutor())
        .add(flowB)
        .next(aggregateStep)
        .end()
        .build();
}
```

### Async Task Executor Configuration
```java
@Configuration
public class AsyncConfig {

    @Bean(name = "batchTaskExecutor")
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}

@Bean
public Job parallelJob(JobRepository jobRepository,
                       Flow flowA,
                       Flow flowB,
                       @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor,
                       Step aggregateStep) {
    return new JobBuilder("parallelJob", jobRepository)
        .start(flowA)
        .split(taskExecutor)
        .add(flowB)
        .next(aggregateStep)
        .end()
        .build();
}
```

## 3.4 Nested Jobs

### Parent Job
```java
@Bean
public Job parentJob(JobRepository jobRepository,
                     Step parentStep1,
                     Job childJob) {
    return new JobBuilder("parentJob", jobRepository)
        .start(parentStep1)
        .next(new JobStep(childJob, jobRepository, transactionManager))
        .build();
}

@Bean
public Step parentStep1(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager) {
    return new StepBuilder("parentStep1", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            System.out.println("Parent job: Step 1");
            return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
}
```

### Child Job
```java
@Configuration
public class ChildJobConfig {

    @Bean
    public Job childJob(JobRepository jobRepository,
                        Step childStep1) {
        return new JobBuilder("childJob", jobRepository)
            .start(childStep1)
            .build();
    }

    @Bean
    public Step childStep1(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager) {
        return new StepBuilder("childStep1", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Child job: Step 1");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
```

## 3.5 Job Parameters Validation

### JobParametersValidator
```java
@Component
public class CustomJobParametersValidator
        implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters)
            throws JobParametersInvalidException {
        // Required parameters
        String filename = parameters.getString("filename");
        if (filename == null || filename.isBlank()) {
            throw new JobParametersInvalidException(
                "Parameter 'filename' is required");
        }

        // Validate file extension
        if (!filename.endsWith(".csv")) {
            throw new JobParametersInvalidException(
                "Filename must have .csv extension");
        }

        // Optional parameters with defaults
        Integer batchSize = parameters.getInt("batch.size", 100);
        if (batchSize < 10 || batchSize > 10000) {
            throw new JobParametersInvalidException(
                "Batch size must be between 10 and 10000");
        }
    }
}
```

### DefaultValidator
```java
@Bean
public Job parameterizedJob(JobRepository jobRepository,
                            Step step1,
                            CustomJobParametersValidator validator) {
    return new JobBuilder("parameterizedJob", jobRepository)
        .validator(validator)
        .start(step1)
        .build();
}
```

### CompositeValidator
```java
@Bean
public Job compositeValidatedJob(JobRepository jobRepository,
                                  Step step1) {
    CompositeJobParametersValidator validator =
        new CompositeJobParametersValidator();

    validator.addValidator(new DefaultJobParametersValidator() {
        @Override
        public void validate(JobParameters parameters)
                throws JobParametersInvalidException {
            // Custom validation
        }
    });

    validator.addValidator(new DateFormatValidator());

    return new JobBuilder("compositeValidatedJob", jobRepository)
        .validator(validator)
        .start(step1)
        .build();
}
```

## 3.6 Job Listener

### @BeforeJob/@AfterJob
```java
@Component
@JobScope
public class JobExecutionListener {

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("=================================");
        System.out.println("JOB STARTED: " + jobExecution.getJobInstance().getJobName());
        System.out.println("Start Time: " + jobExecution.getStartTime());
        System.out.println("Parameters: " + jobExecution.getJobParameters());
        System.out.println("=================================");
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        System.out.println("=================================");
        System.out.println("JOB COMPLETED: " + jobExecution.getStatus());
        System.out.println("End Time: " + jobExecution.getEndTime());
        System.out.println("Exit Status: " + jobExecution.getExitStatus().getExitCode());

        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            System.out.println("FAILURE EXCEPTIONS:");
            jobExecution.getAllFailureExceptions().forEach(ex ->
                System.out.println("  - " + ex.getMessage()));
        }
        System.out.println("=================================");
    }
}
```

### Implementing JobListener
```java
public class CustomJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Logic before job starts
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Logic after job completes
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

## 3.7 Step Listener

### @BeforeStep/@AfterStep
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
        System.out.println("  Read: " + stepExecution.getReadCount());
        System.out.println("  Write: " + stepExecution.getWriteCount());
        System.out.println("  Commit: " + stepExecution.getCommitCount());
        System.out.println("  Rollback: " + stepExecution.getRollbackCount());

        return stepExecution.getExitStatus();
    }
}
```

### Chunk Listener
```java
@Component
public class ChunkListener {

    @BeforeChunk
    public void beforeChunk(ChunkContext context) {
        System.out.println(">>> Starting chunk processing...");
        System.out.println("    Step: " + context.getStepContext().getStepName());
    }

    @AfterChunk
    public void afterChunk(ChunkContext context) {
        System.out.println("<<< Chunk processing completed");
        System.out.println("    Items processed: " +
            context.getStepContext().getStepExecution().getWriteCount());
    }

    @AfterChunkError
    public void afterChunkError(ChunkContext context) {
        System.out.println("!!! Chunk processing failed");
        Throwable exception = context.getException();
        if (exception != null) {
            System.out.println("    Error: " + exception.getMessage());
        }
    }
}
```

### ItemReadListener
```java
@Component
public class ItemReadListener {

    @BeforeRead
    public void beforeRead() {
        System.out.println("Reading next item...");
    }

    @AfterRead
    public void afterRead(Object item) {
        System.out.println("Read item: " + item);
    }

    @OnReadError
    public void onReadError(Exception ex) {
        System.out.println("Error reading item: " + ex.getMessage());
    }
}
```

### ItemProcessListener
```java
@Component
public class ItemProcessListener {

    @BeforeProcess
    public void beforeProcess(Object item) {
        System.out.println("Processing: " + item);
    }

    @AfterProcess
    public void afterProcess(Object item, Object result) {
        System.out.println("Processed: " + item + " -> " + result);
    }

    @OnProcessError
    public void onProcessError(Object item, Exception ex) {
        System.out.println("Error processing: " + item + " - " + ex.getMessage());
    }
}
```

### ItemWriteListener
```java
@Component
public class ItemWriteListener {

    @BeforeWrite
    public void beforeWrite(List<?> items) {
        System.out.println("Writing " + items.size() + " items...");
    }

    @AfterWrite
    public void afterWrite(List<?> items) {
        System.out.println("Written " + items.size() + " items successfully");
    }

    @OnWriteError
    public void onWriteError(Exception ex, List<?> items) {
        System.out.println("Error writing items: " + ex.getMessage());
    }
}
```

## 3.8 Scope Configuration

### @JobScope
```java
@Component
@JobScope
public class JobScopedService {

    @Value("#{jobParameters['input.file']}")
    private String inputFile;

    @Value("#{jobExecutionContext['processing.mode']}")
    private String processingMode;

    public void process() {
        System.out.println("Input file: " + inputFile);
        System.out.println("Processing mode: " + processingMode);
    }
}
```

### @StepScope
```java
@Component
@StepScope
public class StepScopedService {

    @Value("#{stepExecutionContext['current.chunk']}")
    private Integer currentChunk;

    @Value("#{jobParameters['batch.size']}")
    private Integer batchSize;

    public void execute() {
        System.out.println("Current chunk: " + currentChunk);
        System.out.println("Batch size: " + batchSize);
    }
}
```

## 3.9 Job Configuration with Java Config

### Full Configuration Example
```java
@Configuration
public class CompleteJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job completeJob(JobListener jobListener,
                           Step step1,
                           Step step2) {
        return jobBuilderFactory.get("completeJob")
            .listener(jobListener)
            .start(step1)
            .on("COMPLETED").to(step2)
            .from(step1).on("FAILED").end("FAILED")
            .end()
            .build();
    }

    @Bean
    public Step step1(PlatformTransactionManager transactionManager) {
        return stepBuilderFactory.get("completeStep1")
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Step 1 processing...");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .listener(new StepExecutionListener())
            .build();
    }

    @Bean
    public Step step2(PlatformTransactionManager transactionManager) {
        return stepBuilderFactory.get("completeStep2")
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Step 2 processing...");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
```

## 3.10 Practice Scenario

### Scenario: Order Processing Job with Validation
```java
@Configuration
public class OrderProcessingJobConfig {

    @Bean
    public Job orderProcessingJob(
            JobRepository jobRepository,
            Step validateOrderStep,
            Step processOrderStep,
            Step notifyStep,
            JobExecutionListener jobListener) {

        return new JobBuilder("orderProcessingJob", jobRepository)
            .listener(jobListener)
            .start(validateOrderStep)
            .on("VALID").to(processOrderStep)
            .from(validateOrderStep).on("INVALID").to(notifyStep).end()
            .from(processOrderStep).on("COMPLETED").to(notifyStep)
            .end()
            .build();
    }

    @Bean
    public Step validateOrderStep(JobRepository jobRepository,
                                  PlatformTransactionManager txManager) {
        return new StepBuilder("validateOrderStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                String orderId = chunkContext.getStepContext()
                    .getJobParameters().getString("orderId");

                System.out.println("Validating order: " + orderId);

                // Simulate validation
                boolean isValid = orderId != null && !orderId.isBlank();

                contribution.setExitStatus(isValid ?
                    ExitStatus("VALID") : ExitStatus("INVALID"));

                return RepeatStatus.FINISHED;
            }, txManager)
            .build();
    }

    @Bean
    public Step notifyStep(JobRepository jobRepository,
                           PlatformTransactionManager txManager) {
        return new StepBuilder("notifyStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                String orderId = chunkContext.getStepContext()
                    .getJobParameters().getString("orderId");
                ExitStatus status = chunkContext.getStepContext()
                    .getStepExecution().getExitStatus();

                System.out.println("Notifying for order: " + orderId);
                System.out.println("Status: " + status.getExitCode());

                return RepeatStatus.FINISHED;
            }, txManager)
            .build();
    }
}
```

## 3.11 Summary

| Feature | Description | Usage |
|---------|-------------|-------|
| Conditional Flow | Branch based on ExitStatus | `.on("STATUS").to(nextStep)` |
| JobDecider | Custom decision logic | Implements `JobExecutionDecider` |
| Split | Parallel execution | `.split(taskExecutor).add(flow1, flow2)` |
| Nested Job | Job calling job | `new JobStep(childJob)` |
| Validation | Parameter validation | Implements `JobParametersValidator` |
| Listeners | Hooks for lifecycle events | `@BeforeJob`, `@AfterStep`, etc. |
| Scoping | Dynamic value injection | `@JobScope`, `@StepScope` |

## 3.12 Next Steps

- [Chapter 4: Step Types](04-step-types.md)
- Learn about Tasklet and Chunk-oriented processing
- Understand chunk size and commit intervals

## Exercises

### Exercise 1: Retry Flow
Create a job that:
1. Runs a step
2. If fails, retries up to 3 times
3. After all retries fail, goes to error handling step

### Exercise 2: Parallel Processing
Create a job that:
1. Processes multiple files in parallel
2. Aggregates results after all complete
3. Uses ThreadPoolTaskExecutor

### Exercise 3: Custom Decider
Create a decider that:
1. Checks the time of day
2. Routes to different steps based on time
3. Logs the decision

---
*Duration: 1 hour*
