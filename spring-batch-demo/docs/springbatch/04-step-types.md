# Chapter 4: Step Types

## Overview
Learn about Tasklet-based and Chunk-oriented step processing.

## 4.1 Step Types Overview

```
Step
├── TaskletStep (Simple task)
│   └── Tasklet (Single unit of work)
└── Chunk-oriented Step (Item processing)
    ├── ItemReader (Read data)
    ├── ItemProcessor (Transform data)
    └── ItemWriter (Write data)
```

## 4.2 TaskletStep

### Simple Tasklet
```java
@Bean
public Step simpleTaskletStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
    return new StepBuilder("simpleTaskletStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            System.out.println("Executing simple tasklet...");
            System.out.println("Step: " +
                chunkContext.getStepContext().getStepName());
            System.out.println("Job Parameters: " +
                chunkContext.getStepContext().getJobParameters());
            return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
}
```

### Tasklet with Loop
```java
@Bean
public Step loopingTaskletStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
    return new StepBuilder("loopingTaskletStep", jobRepository)
        .tasklet(loopingTasklet(), transactionManager)
        .build();
}

private Tasklet loopingTasklet() {
    return (contribution, chunkContext) -> {
        int count = 0;
        for (int i = 0; i < 10; i++) {
            System.out.println("Processing item " + i);
            count++;
        }
        System.out.println("Processed " + count + " items");
        return RepeatStatus.FINISHED;
    };
}
```

### Tasklet with ExecutionContext
```java
@Bean
public Step contextTaskletStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
    return new StepBuilder("contextTaskletStep", jobRepository)
        .tasklet(contextTasklet(), transactionManager)
        .build();
}

private Tasklet contextTasklet() {
    return (contribution, chunkContext) -> {
        ExecutionContext jobContext = chunkContext.getStepContext()
            .getJobExecutionContext();
        ExecutionContext stepContext = chunkContext.getStepContext()
            .getStepExecution().getExecutionContext();

        // Store data
        jobContext.put("sharedData", "This is shared across steps");
        stepContext.put("stepData", "This is step-specific");

        // Retrieve and use
        String jobData = (String) jobContext.get("sharedData");
        String stepData = (String) stepContext.get("stepData");

        System.out.println("Job context data: " + jobData);
        System.out.println("Step context data: " + stepData);

        return RepeatStatus.FINISHED;
    };
}
```

## 4.3 Chunk-oriented Processing

### Basic Chunk Configuration
```java
@Bean
public Step chunkStep(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemReader<String> itemReader,
                      ItemProcessor<String, String> itemProcessor,
                      ItemWriter<String> itemWriter) {
    return new StepBuilder("chunkStep", jobRepository)
        .<String, String>chunk(10, transactionManager)  // Chunk size = 10
        .reader(itemReader)
        .processor(itemProcessor)
        .writer(itemWriter)
        .build();
}
```

### Understanding Chunk Processing
```
Chunk Processing Flow:

ItemReader      ItemProcessor      ItemWriter
   │                │                │
   ├─ Item 1 ──────►│                │
   ├─ Item 2 ──────►│                │
   ├─ Item 3 ──────►│                │
   │      ...       │                │
   ├─ Item 10 ─────►│                │
   │                │                │
   │                └─ Chunk 1 ─────►│ (Transaction)
   │                                   (Commit)
   ├─ Item 11 ─────►│                │
   │      ...       │                │
   ├─ Item 20 ─────►│                │
   │                └─ Chunk 2 ─────►│ (Transaction)
   │                                   (Commit)
```

### Chunk Size Considerations
```java
// Small chunk - frequent commits, lower memory
@Bean
public Step smallChunkStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<String> reader,
                           ItemWriter<String> writer) {
    return new StepBuilder("smallChunkStep", jobRepository)
        .<String, String>chunk(5, transactionManager)
        .reader(reader)
        .writer(writer)
        .build();
}

// Large chunk - fewer commits, higher memory
@Bean
public Step largeChunkStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<String> reader,
                           ItemWriter<String> writer) {
    return new StepBuilder("largeChunkStep", jobRepository)
        .<String, String>chunk(1000, transactionManager)
        .reader(reader)
        .writer(writer)
        .build();
}
```

### FaultTolerant Chunk
```java
@Bean
public Step faultTolerantStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              ItemReader<String> reader,
                              ItemWriter<String> writer) {
    return new StepBuilder("faultTolerantStep", jobRepository)
        .<String, String>chunk(10, transactionManager)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .skip(Exception.class)           // Skip on exception
        .skipLimit(5)                     // Allow 5 skips
        .retry(Exception.class)           // Retry on exception
        .retryLimit(3)                    // Retry 3 times
        .build();
}
```

## 4.4 Tasklet Implementation Patterns

### File Processing Tasklet
```java
@Component
public class FileProcessingTasklet implements Tasklet {

    @Value("#{jobParameters['input.file']}")
    private String inputFile;

    @Value("#{jobParameters['output.file']}")
    private String outputFile;

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext)
            throws Exception {
        Path inputPath = Paths.get(inputFile);
        Path outputPath = Paths.get(outputFile);

        // Read input
        List<String> lines = Files.readAllLines(inputPath);

        // Process
        List<String> processed = lines.stream()
            .map(this::processLine)
            .collect(Collectors.toList());

        // Write output
        Files.write(outputPath, processed);

        // Update execution context
        ExecutionContext ec = chunkContext.getStepContext()
            .getStepExecution().getExecutionContext();
        ec.putInt("lines.processed", processed.size());

        return RepeatStatus.FINISHED;
    }

    private String processLine(String line) {
        return line.toUpperCase().trim();
    }
}
```

### Database Tasklet
```java
@Component
public class DatabaseMaintenanceTasklet implements Tasklet {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("#{jobParameters['operation']}")
    private String operation;

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext)
            throws Exception {
        switch (operation) {
            case "archive":
                return archiveOldData();
            case "cleanup":
                return cleanupData();
            case "rebuild":
                return rebuildIndexes();
            default:
                throw new IllegalArgumentException(
                    "Unknown operation: " + operation);
        }
    }

    private RepeatStatus archiveOldData() {
        int deleted = jdbcTemplate.update(
            "INSERT INTO archived_orders SELECT * FROM orders " +
            "WHERE order_date < DATE_SUB(NOW(), INTERVAL 1 YEAR)");
        jdbcTemplate.update(
            "DELETE FROM orders WHERE order_date < DATE_SUB(NOW(), INTERVAL 1 YEAR)");

        System.out.println("Archived " + deleted + " orders");
        return RepeatStatus.FINISHED;
    }

    private RepeatStatus cleanupData() {
        // Cleanup logic
        return RepeatStatus.FINISHED;
    }

    private RepeatStatus rebuildIndex() {
        // Rebuild index logic
        return RepeatStatus.FINISHED;
    }
}
```

### API Call Tasklet
```java
@Component
public class ApiSyncTasklet implements Tasklet {

    @Autowired
    private RestTemplate restTemplate;

    @Value("#{jobParameters['api.url']}")
    private String apiUrl;

    @Value("#{jobParameters['batch.size']}")
    private int batchSize;

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext)
            throws Exception {
        int page = 0;
        int totalSynced = 0;

        while (true) {
            List<String> data = fetchBatch(page);

            if (data.isEmpty()) {
                break;
            }

            processBatch(data);
            totalSynced += data.size();
            page++;

            // Update progress
            contribution.incrementReadCount();
        }

        chunkContext.getStepContext().getStepExecution()
            .getExecutionContext()
            .putInt("total.synced", totalSynced);

        return RepeatStatus.FINISHED;
    }

    private List<String> fetchBatch(int page) {
        String url = apiUrl + "?page=" + page + "&size=" + batchSize;
        ResponseEntity<List> response = restTemplate.exchange(
            url, HttpMethod.GET, null, List.class);
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    private void processBatch(List<String> data) {
        // Process data
    }
}
```

### Multi-threaded Tasklet
```java
@Component
public class ParallelTasklet implements Tasklet {

    @Autowired
    private ExecutorService executorService;

    @Value("#{jobParameters['threads']}")
    private int threadCount;

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext)
            throws Exception {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            tasks.add(() -> processThread(threadId));
        }

        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        int total = futures.stream()
            .mapToInt(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();

        System.out.println("Total processed: " + total);
        return RepeatStatus.FINISHED;
    }

    private int processThread(int threadId) {
        // Process work for this thread
        return threadId * 100;
    }
}
```

## 4.5 Chunk Listener Integration

```java
@Component
public class ProcessingChunkListener {

    @BeforeChunk
    public void beforeChunk(ChunkContext context) {
        long startTime = System.currentTimeMillis();
        context.getStepContext().getStepExecution()
            .getExecutionContext()
            .putLong("chunk.start.time", startTime);

        System.out.println("Starting chunk at: " + startTime);
    }

    @AfterChunk
    public void afterChunk(ChunkContext context) {
        long startTime = context.getStepContext().getStepExecution()
            .getExecutionContext().getLong("chunk.start.time");
        long duration = System.currentTimeMillis() - startTime;

        int itemsProcessed = context.getStepContext()
            .getStepExecution().getWriteCount();

        System.out.println("Chunk completed in " + duration + "ms");
        System.out.println("Items processed: " + itemsProcessed);
    }

    @AfterChunkError
    public void afterChunkError(ChunkContext context) {
        Throwable exception = context.getException();
        System.out.println("Chunk failed with error: " +
            exception != null ? exception.getMessage() : "Unknown");
    }
}
```

## 4.6 Step Configuration Options

### Allow Start If Complete
```java
@Bean
public Step restartableStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager) {
    return new StepBuilder("restartableStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            // This step will always run, even if job is restarted
            return RepeatStatus.FINISHED;
        }, transactionManager)
        .allowStartIfComplete(true)  // Always run
        .build();
}
```

### Start Limit
```java
@Bean
public Step limitedStep(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager) {
    return new StepBuilder("limitedStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            return RepeatStatus.FINISHED;
        }, transactionManager)
        .startLimit(3)  // Allow only 3 executions
        .build();
}
```

### Injecting into Tasklet
```java
@Component
@StepScope
public class InjectedTasklet implements Tasklet {

    @Value("#{jobParameters['message']}")
    private String message;

    @Value("#{stepExecutionContext['iteration']}")
    private Integer iteration;

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext)
            throws Exception {
        System.out.println("Message: " + message);
        System.out.println("Iteration: " + iteration);
        return RepeatStatus.FINISHED;
    }
}
```

## 4.7 Practice Scenario

### Scenario: Report Generation Job

```java
@Configuration
public class ReportGenerationJobConfig {

    @Bean
    public Job reportGenerationJob(JobRepository jobRepository,
                                   Step extractDataStep,
                                   Step generateReportStep,
                                   Step distributeReportStep) {
        return new JobBuilder("reportGenerationJob", jobRepository)
            .start(extractDataStep)
            .next(generateReportStep)
            .next(distributeReportStep)
            .build();
    }

    @Bean
    public Step extractDataStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        return new StepBuilder("extractDataStep", jobRepository)
            .tasklet(extractDataTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Step generateReportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("generateReportStep", jobRepository)
            .<ReportData, ReportData>chunk(100, transactionManager)
            .reader(reportDataReader())
            .processor(reportDataProcessor())
            .writer(reportWriter())
            .listener(reportChunkListener())
            .build();
    }

    @Bean
    public Step distributeReportStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager) {
        return new StepBuilder("distributeReportStep", jobRepository)
            .tasklet(distributeReportTasklet(), transactionManager)
            .build();
    }
}

@Component
class ExtractDataTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws Exception {
        String reportType = chunkContext.getStepContext()
            .getJobParameters().getString("report.type");

        System.out.println("Extracting data for " + reportType + " report...");

        // Simulate data extraction
        Thread.sleep(1000);

        chunkContext.getStepContext().getStepExecution()
            .getExecutionContext()
            .put("extracted.records", 1500);

        System.out.println("Data extraction completed");
        return RepeatStatus.FINISHED;
    }
}

@Component
class ReportDataProcessor implements ItemProcessor<ReportData, ReportData> {
    @Override
    public ReportData process(ReportData item) throws Exception {
        // Process and format data
        item.setProcessed(true);
        item.setFormattedValue(String.format("%.2f", item.getValue()));
        return item;
    }
}

@Component
class ReportChunkListener {
    @BeforeChunk
    public void beforeChunk(ChunkContext context) {
        System.out.println(">>> Generating report chunk...");
    }

    @AfterChunk
    public void afterChunk(ChunkContext context) {
        int written = context.getStepContext().getStepExecution()
            .getWriteCount();
        System.out.println("<<< Chunk completed: " + written + " records");
    }
}
```

## 4.8 Summary

| Feature | Tasklet | Chunk-oriented |
|---------|---------|----------------|
| Use Case | Simple, one-time operations | Processing large datasets |
| Transaction | Single transaction | Chunk-based transactions |
| Memory | Lower for simple tasks | Higher, depends on chunk size |
| Error Handling | Manual | Built-in skip/retry |
| Processing | Sequential | Parallel with readers/writers |

## 4.9 Next Steps

- [Chapter 5: Item Readers](05-item-reader.md)
- Learn about JDBC, MongoDB, file readers
- Implement custom readers

## Exercises

### Exercise 1: File Archive Tasklet
Create a tasklet that:
1. Reads a directory from parameters
2. Moves files older than 30 days to archive
3. Logs the number of files archived

### Exercise 2: Chunk Processing
Create a chunk step that:
1. Reads numbers from a list
2. Processes them (e.g., doubles the value)
3. Writes to console
4. Experiment with different chunk sizes

### Exercise 3: Error Handling
Add to the chunk step:
1. Skip policy for invalid numbers
2. Retry policy for transient errors
3. Limit to 3 skips and 2 retries

---
*Duration: 1.5 hours*
