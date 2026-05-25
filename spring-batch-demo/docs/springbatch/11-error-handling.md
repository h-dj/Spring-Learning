# Chapter 11: Error Handling

## Overview
Master retry, skip, restart, and fault tolerance mechanisms in Spring Batch.

## 11.1 Error Handling Overview

```
Error Handling Mechanisms
├── Skip Policy (Skip specific exceptions)
├── Retry Policy (Retry failed operations)
├── Restart (Resume from last successful step)
├── RetryTemplate (Programmatic retry)
└── Listeners (Error notifications)
```

## 11.2 Skip Configuration

### Basic Skip
```java
@Bean
public Step basicSkipStep(JobRepository jobRepository,
                          PlatformTransactionManager txManager,
                          ItemReader<String> reader,
                          ItemWriter<String> writer) {

    return new StepBuilder("basicSkipStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .skip(Exception.class)           // Skip any exception
        .skipLimit(5)                    // Allow 5 skips per run
        .build();
}
```

### Specific Exceptions
```java
@Bean
public Step specificSkipStep(JobRepository jobRepository,
                             PlatformTransactionManager txManager,
                             ItemReader<String> reader,
                             ItemWriter<String> writer) {

    return new StepBuilder("specificSkipStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .skip(DataIntegrityViolationException.class)
        .skip(ParseException.class)
        .skip(ValidationException.class)
        .skipLimit(10)
        .build();
}
```

### Skip Limit per Exception
```java
@Bean
public Step multiSkipStep(JobRepository jobRepository,
                          PlatformTransactionManager txManager,
                          ItemReader<String> reader,
                          ItemWriter<String> writer) {

    return new StepBuilder("multiSkipStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .skip(ValidationException.class)
        .skipLimit(5)        // 5 validation errors allowed
        .skip(DataIntegrityViolationException.class)
        .skipLimit(2)        // 2 integrity violations allowed
        .build();
}
```

### Custom Skip Policy
```java
public class CustomSkipPolicy implements SkipPolicy {

    private final int maxSkip;
    private final Set<Class<? extends Throwable>> skippable;

    public CustomSkipPolicy(int maxSkip,
                           Set<Class<? extends Throwable>> skippable) {
        this.maxSkip = maxSkip;
        this.skippable = skippable;
    }

    @Override
    public boolean shouldSkip(Throwable t, int skipCount)
            throws SkipLimitExceededException {

        if (skipCount >= maxSkip) {
            throw new SkipLimitExceededException(
                "Skip limit exceeded. Max: " + maxSkip,
                t, skipCount);
        }

        for (Class<? extends Throwable> skippableEx : skippable) {
            if (skippableEx.isInstance(t)) {
                return true;
            }
        }

        return false;
    }
}

@Bean
public Step customPolicyStep(JobRepository jobRepository,
                             PlatformTransactionManager txManager,
                             ItemReader<String> reader,
                             ItemWriter<String> writer) {

    Set<Class<? extends Throwable>> skippable = new HashSet<>();
    skippable.add(ValidationException.class);
    skippable.add(ParseException.class);

    CustomSkipPolicy policy = new CustomSkipPolicy(10, skippable);

    return new StepBuilder("customPolicyStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .skipPolicy(policy)
        .build();
}
```

## 11.3 Retry Configuration

### Basic Retry
```java
@Bean
public Step basicRetryStep(JobRepository jobRepository,
                           PlatformTransactionManager txManager,
                           ItemReader<String> reader,
                           ItemProcessor<String, String> processor,
                           ItemWriter<String> writer) {

    return new StepBuilder("basicRetryStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .retry(Exception.class)           // Retry any exception
        .retryLimit(3)                    // Retry up to 3 times
        .build();
}
```

### Specific Retry Exceptions
```java
@Bean
public Step selectiveRetryStep(JobRepository jobRepository,
                               PlatformTransactionManager txManager,
                               ItemReader<String> reader,
                               ItemProcessor<String, String> processor,
                               ItemWriter<String> writer) {

    return new StepBuilder("selectiveRetryStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .retry(TransientDataAccessException.class)
        .retry(TimeoutException.class)
        .retryLimit(3)
        .build();
}
```

### Retry with Backoff
```java
@Bean
public Step backoffRetryStep(JobRepository jobRepository,
                             PlatformTransactionManager txManager,
                             ItemReader<String> reader,
                             ItemProcessor<String, String> processor,
                             ItemWriter<String> writer) {

    return new StepBuilder("backoffRetryStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .retry(Exception.class)
        .retryLimit(3)
        .retryBackoffMultiplier(2)        // Double delay each retry
        .retryInitialInterval(1000)       // Initial: 1 second
        .retryMaxInterval(10000)          // Max: 10 seconds
        .build();
}
```

### Custom Retry Policy
```java
@Bean
public Step customRetryStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager,
                            ItemReader<String> reader,
                            ItemWriter<String> writer) {

    return new StepBuilder("customRetryStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .retryTemplate(customRetryTemplate())
        .build();
}

private RetryTemplate customRetryTemplate() {
    RetryTemplate template = new RetryTemplate();

    // Fixed backoff
    FixedBackOffPolicy backOff = new FixedBackOffPolicy();
    backOff.setInitialInterval(1000);
    backOff.setInterval(2000);
    backOff.setMaxAttempts(3);
    template.setBackOffPolicy(backOff);

    // Simple retry policy
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(3);
    retryPolicy.setRetryOn(TransientException.class);
    template.setRetryPolicy(retryPolicy);

    return template;
}
```

## 11.4 RetryTemplate Usage

### Programmatic Retry
```java
@Service
public class RetryService {

    @Autowired
    private RetryTemplate retryTemplate;

    public String retryableOperation(String input) {
        return retryTemplate.execute(context -> {
            // Main operation
            return externalService.process(input);
        }, context -> {
            // Recovery callback
            System.out.println("Retry exhausted after " +
                context.getRetryCount() + " attempts");
            return "fallback-value";
        });
    }

    public <T> T retryWithCustomPolicy(String input,
                                       Class<T> returnType) {
        RetryPolicy retryPolicy = new SimpleRetryPolicy(3,
            Collections.singletonMap(TransientException.class, true));

        BackOffPolicy backOff = new ExponentialBackOffPolicy();
        ((ExponentialBackOffPolicy) backOff).setInitialInterval(1000);
        ((ExponentialBackOffPolicy) backOff).setMultiplier(2);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOff);

        return template.execute(
            context -> externalService.process(input));
    }
}
```

### RetryListener
```java
@Component
public class CustomRetryListener {

    @BeforeRetry
    public void beforeRetry(RetryContext context) {
        System.out.println("Retry attempt: " + context.getRetryCount());
    }

    @AfterRetry
    public void afterRetry(RetryContext context) {
        System.out.println("Retry completed, attempts: " +
            context.getRetryCount());
    }

    @OnRetrySuccess
    public void onSuccess(RetryContext context, Object result) {
        System.out.println("Retry succeeded with result: " + result);
    }

    @OnRetryExhausted
    public void onExhausted(RetryContext context, Throwable t) {
        System.out.println("Retry exhausted: " + t.getMessage());
    }
}
```

## 11.5 Restart Configuration

### Allow Start If Complete
```java
@Bean
public Step restartableStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager,
                            ItemReader<String> reader,
                            ItemWriter<String> writer) {

    return new StepBuilder("restartableStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .allowStartIfComplete(true)  // Run even if previously completed
        .build();
}
```

### Start Limit
```java
@Bean
public Step limitedStartStep(JobRepository jobRepository,
                             PlatformTransactionManager txManager,
                             ItemReader<String> reader,
                             ItemWriter<String> writer) {

    return new StepBuilder("limitedStartStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .writer(writer)
        .startLimit(3)  // Allow only 3 executions
        .build();
}
```

### Restart Scenario
```java
@Configuration
public class RestartableJobConfig {

    @Bean
    public Job restartableJob(JobRepository jobRepository,
                              Step step1,
                              Step step2) {
        return new JobBuilder("restartableJob", jobRepository)
            .start(step1)
            .next(step2)
            .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager txManager) {
        return new StepBuilder("step1", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                // Step 1 logic
                return RepeatStatus.FINISHED;
            }, txManager)
            .allowStartIfComplete(false)  // Default: don't rerun
            .build();
    }

    @Bean
    public Step step2(JobRepository jobRepository,
                      PlatformTransactionManager txManager) {
        return new StepBuilder("step2", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                // Step 2 logic
                return RepeatStatus.FINISHED;
            }, txManager)
            .allowStartIfComplete(true)  // Always run
            .build();
    }
}
```

### ExecutionContext Persistence
```java
@Component
@StepScope
public class StatefulTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext)
            throws Exception {

        ExecutionContext ec = chunkContext.getStepContext()
            .getStepExecution().getExecutionContext();

        // Restore state
        int processedCount = ec.getInt("processedCount", 0);

        // Continue processing
        for (int i = processedCount; i < 100; i++) {
            processItem(i);
            processedCount++;
            ec.putInt("processedCount", processedCount);

            // Save state periodically
            if (i % 10 == 0) {
                contribution.setExitStatus(ExitStatus.EXECUTING);
                return RepeatStatus.CONTINUABLE;
            }
        }

        return RepeatStatus.FINISHED;
    }
}
```

## 11.6 Fault Tolerance Patterns

### Circuit Breaker
```java
@Component
@StepScope
public class CircuitBreakerProcessor
        implements ItemProcessor<String, String> {

    private final CircuitBreaker circuitBreaker;

    @Override
    public String process(String item) throws Exception {
        return circuitBreaker.execute(() -> {
            // Potentially failing operation
            return externalService.process(item);
        });
    }
}

@Component
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreaker circuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .build();

        return CircuitBreakerRegistry.of(config)
            .circuitBreaker("batchCircuitBreaker");
    }
}
```

### Fallback Handler
```java
@Component
@StepScope
public class FallbackProcessor
        implements ItemProcessor<String, String> {

    @Override
    public String process(String item) {
        try {
            return externalService.process(item);
        } catch (TransientException e) {
            // Use fallback value
            return "fallback-" + item;
        }
    }
}
```

### Dead Letter Queue
```java
@Component
@StepScope
public class DlqItemWriter implements ItemWriter<Item> {

    private final JdbcBatchItemWriter<Item> delegate;
    private final JdbcTemplate errorJdbcTemplate;

    @Override
    public void write(Chunk<? extends Item> chunk) throws Exception {
        try {
            delegate.write(chunk);
        } catch (Exception e) {
            // Log to error queue
            for (Item item : chunk) {
                saveToDeadLetterQueue(item, e);
            }
        }
    }

    private void saveToDeadLetterQueue(Item item, Exception e) {
        errorJdbcTemplate.update(
            "INSERT INTO dead_letter_queue (item_data, error_message, created_at) " +
            "VALUES (?, ?, ?)",
            serialize(item), e.getMessage(), new Date());
    }
}
```

## 11.7 Skip and Retry Combination

```java
@Bean
public Step combinedStep(JobRepository jobRepository,
                         PlatformTransactionManager txManager,
                         ItemReader<String> reader,
                         ItemProcessor<String, String> processor,
                         ItemWriter<String> writer) {

    return new StepBuilder("combinedStep", jobRepository)
        .<String, String>chunk(10, txManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .skip(Exception.class)
        .skipLimit(5)
        .retry(Exception.class)
        .retryLimit(3)
        .retryBackoffMultiplier(2)
        .build();
}
```

## 11.8 Error Handling Listeners

```java
@Component
public class ErrorHandlingListener {

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getStatus() == BatchStatus.FAILED) {
            System.err.println("Step failed: " +
                stepExecution.getStepName());
            System.err.println("Rollback count: " +
                stepExecution.getRollbackCount());
        }
        return stepExecution.getExitStatus();
    }
}

@Component
public class SkipErrorListener implements SkipListener<Object, Object> {

    private final Map<String, AtomicInteger> skipCounts =
        new ConcurrentHashMap<>();

    @Override
    public void onSkipInRead(Throwable t) {
        incrementSkipCount("read", t);
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        incrementSkipCount("process", t);
        saveSkippedItem(item, t);
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        incrementSkipCount("write", t);
    }

    private void incrementSkipCount(String type, Throwable t) {
        String key = type + "." + t.getClass().getSimpleName();
        skipCounts.computeIfAbsent(key, k -> new AtomicInteger())
            .incrementAndGet();
    }

    private void saveSkippedItem(Object item, Throwable t) {
        // Save to error table
    }

    public Map<String, Integer> getSkipCounts() {
        return skipCounts.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get()));
    }
}
```

## 11.9 Monitoring Error Handling

```java
@Component
public class ErrorMonitoringService {

    @Autowired
    private JobRepository jobRepository;

    @Scheduled(fixedRate = 60000)
    public void monitorErrors() {
        List<JobExecution> runningExecutions = getRunningExecutions();

        for (JobExecution execution : runningExecutions) {
            for (StepExecution stepExecution :
                    execution.getStepExecutions()) {

                int skipCount = stepExecution.getSkipCount();
                int rollbackCount = stepExecution.getRollbackCount();

                if (rollbackCount > 5) {
                    sendAlert("High rollback count for step: " +
                        stepExecution.getStepName());
                }

                if (skipCount > 100) {
                    sendAlert("High skip count for step: " +
                        stepExecution.getStepName());
                }
            }
        }
    }

    private void sendAlert(String message) {
        // Send to monitoring system
    }
}
```

## 11.10 Practice Scenario

### Scenario: Resilient Data Import Job
```java
@Configuration
public class ResilientImportJobConfig {

    @Bean
    public Job resilientImportJob(JobRepository jobRepository,
                                  Step importStep) {
        return new JobBuilder("resilientImportJob", jobRepository)
            .start(importStep)
            .build();
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager txManager,
                           ItemReader<Customer> reader,
                           ItemProcessor<Customer, Customer> processor,
                           ItemWriter<Customer> writer) {

        return new StepBuilder("importStep", jobRepository)
            .<Customer, Customer>chunk(100, txManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skip(ValidationException.class)
            .skipLimit(50)
            .retry(DataAccessException.class)
            .retryLimit(3)
            .retryBackoffMultiplier(2)
            .listener(new SkipErrorListener())
            .build();
    }
}

@Component
class SkipErrorListener implements SkipListener<Customer, Customer> {

    private final Map<String, Integer> skipCounts = new ConcurrentHashMap<>();

    @Override
    public void onSkipInRead(Throwable t) {
        skipCounts.merge("read." + t.getClass().getSimpleName(), 1, Integer::sum);
    }

    @Override
    public void onSkipInProcess(Customer item, Throwable t) {
        String key = "process." + t.getClass().getSimpleName();
        skipCounts.merge(key, 1, Integer::sum);
        saveToErrorTable(item, t);
    }

    @Override
    public void onSkipInWrite(Customer item, Throwable t) {
        skipCounts.merge("write." + t.getClass().getSimpleName(), 1, Integer::sum);
    }

    private void saveToErrorTable(Customer item, Throwable t) {
        // Save to error table for later investigation
    }
}
```

## 11.11 Summary

| Feature | Purpose | Key Configuration |
|---------|---------|-------------------|
| Skip | Skip problematic items | `.skip(Exception.class).skipLimit(n)` |
| Retry | Retry failed operations | `.retry(Exception.class).retryLimit(n)` |
| Restart | Resume from checkpoint | `.allowStartIfComplete(true)` |
| Start Limit | Limit executions | `.startLimit(n)` |
| RetryTemplate | Programmatic retry | `RetryTemplate.execute()` |
| Circuit Breaker | Prevent cascade failure | Resilience4j |

## 11.12 Next Steps

- [Chapter 12: Advanced Features](12-advanced-features.md)
- Learn partitioning and parallel processing
- Scale batch applications

## Exercises

### Exercise 1: Skip Configuration
Create a job that:
1. Skips validation errors
2. Has different limits for different exceptions
3. Logs skipped items

### Exercise 2: Retry with Backoff
Implement retry that:
1. Uses exponential backoff
2. Retries transient failures
3. Has fallback behavior

### Exercise 3: Restart Capability
Create a job that:
1. Persists state in ExecutionContext
2. Can restart from where it failed
3. Uses allowStartIfComplete

---
*Duration: 2 hours*
