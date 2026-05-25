# Chapter 1: Quick Start with Spring Batch

## Overview
Set up your first Spring Batch application and run a simple batch job.

## 1.1 What is Spring Batch?

Spring Batch is a lightweight, comprehensive batch framework designed for robust batch processing. Key features:
- **Transaction Management**: Built-in transaction handling
- **Chunk-based Processing**: Efficient memory usage for large datasets
- **Restartability**: Jobs can be restarted from where they failed
- **Resource Management**: Efficient handling of resources
- **Statistics**: Built-in job and step execution statistics

## 1.2 Project Setup

### Add Dependencies
```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- Spring Batch -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>

    <!-- Database (H2 for demo) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Application Properties
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:h2:mem:batchdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false  # Disable auto-run on startup
  h2:
    console:
      enabled: true

logging:
  level:
    org.springframework.batch: INFO
```

## 1.3 Your First Batch Job

### Simple Tasklet Job
```java
package cn.reid.springbatchdemo.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class FirstJobConfig {

    @Bean
    public Job firstJob(JobRepository jobRepository, Step firstStep) {
        return new JobBuilder("firstJob", jobRepository)
            .start(firstStep)
            .build();
    }

    @Bean
    public Step firstStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager) {
        return new StepBuilder("firstStep", jobRepository)
            .tasklet(firstTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet firstTasklet() {
        return (contribution, chunkContext) -> {
            System.out.println("=================================");
            System.out.println("Hello Spring Batch!");
            System.out.println("Current Step: " +
                chunkContext.getStepContext().getStepName());
            System.out.println("Job Parameters: " +
                chunkContext.getStepContext().getJobParameters());
            System.out.println("=================================");
            return RepeatStatus.FINISHED;
        };
    }
}
```

## 1.4 Launching the Job

### Using CommandLineRunner
```java
package cn.reid.springbatchdemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBatchDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner launchJob(JobLauncher jobLauncher,
                                       Job firstJob) {
        return args -> {
            System.out.println("Starting first batch job...");
            jobLauncher.run(firstJob, new JobParameters());
            System.out.println("Batch job completed!");
        };
    }
}
```

### Using JobLauncher Directly
```java
@Service
public class JobLauncherService {

    private final JobLauncher jobLauncher;
    private final Job firstJob;

    public void runJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("message", "Hello Batch!")
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncher.run(firstJob, params);
        System.out.println("Job Status: " + execution.getStatus());
    }
}
```

## 1.5 Viewing Job Execution

### Spring Boot Actuator
```yaml
# Add to pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: batch
  info:
    env:
      enabled: true
```

### REST Endpoints
```bash
# List all job executions
curl http://localhost:8080/actuator/batch/jobs

# Get specific job execution
curl http://localhost:8080/actuator/batch/jobs/{jobExecutionId}
```

## 1.6 Understanding the Output

When you run the application, you should see:
```
Starting first batch job...
=================================
Hello Spring Batch!
Current Step: firstStep
Job Parameters: {message=Hello Batch!, run.id=1710000000000}
=================================
Batch job completed!
```

### Check Metadata Tables
Access H2 Console at `http://localhost:8080/h2-console`:
```sql
-- View job instances
SELECT * FROM BATCH_JOB_INSTANCE;

-- View job executions
SELECT * FROM BATCH_JOB_EXECUTION;

-- View step executions
SELECT * FROM BATCH_STEP_EXECUTION;
```

## 1.7 Practice Scenario

### Scenario: File Processing Job
Create a batch job that:
1. Reads from a text file
2. Processes each line
3. Logs the processed data

```java
@Configuration
public class FileProcessingJobConfig {

    @Bean
    public Job fileProcessingJob(JobRepository jobRepository,
                                 Step processFileStep) {
        return new JobBuilder("fileProcessingJob", jobRepository)
            .start(processFileStep)
            .build();
    }

    @Bean
    public Step processFileStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        return new StepBuilder("processFileStep", jobRepository)
            .tasklet(fileProcessingTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet fileProcessingTasklet() {
        return (contribution, chunkContext) -> {
            String filename = chunkContext.getStepContext()
                .getJobParameters().get("filename") != null
                    ? chunkContext.getStepContext()
                        .getJobParameters().get("filename").toString()
                    : "data.txt";

            System.out.println("Processing file: " + filename);
            System.out.println("=================================");

            // Simulate reading and processing
            for (int i = 1; i <= 5; i++) {
                System.out.println("Line " + i + ": Processed successfully");
            }

            System.out.println("=================================");
            System.out.println("File processing completed!");
            return RepeatStatus.FINISHED;
        };
    }
}
```

### Run with Parameters
```java
JobParameters params = new JobParametersBuilder()
    .addString("filename", "customer-data.csv")
    .addLong("timestamp", System.currentTimeMillis())
    .toJobParameters();
```

## 1.8 Key Takeaways

| Concept | Description |
|---------|-------------|
| Job | Container for batch processing logic |
| Step | Single phase of a job |
| Tasklet | Simple unit of work |
| JobRepository | Stores execution metadata |
| JobLauncher | Initiates job execution |

## 1.9 Common Issues

### Issue: Job not running
**Solution**: Ensure `spring.batch.job.enabled=false` if manually launching

### Issue: No transaction manager
**Solution**: Inject `PlatformTransactionManager` into Step builder

### Issue: H2 tables not created
**Solution**: Set `spring.batch.jdbc.initialize-schema=always`

## 1.10 Next Steps

After completing this chapter:
- Move to [Chapter 2: Core Concepts](02-core-concepts.md)
- Learn about JobRepository and execution tracking
- Understand metadata tables

## Exercises

### Exercise 1: Modify the Tasklet
Modify `firstTasklet()` to:
1. Accept a name parameter
2. Print a personalized greeting
3. Display current timestamp

### Exercise 2: Add Multiple Steps
Create a job with two steps:
1. Step 1: Print "Starting..."
2. Step 2: Print "Completed!"

### Exercise 3: Job with Parameters
Create a job that accepts:
- `input.file`: Name of file to process
- `output.file`: Name of output file
- `mode`: Processing mode (e.g., "full", "incremental")

---
*Duration: 30 minutes*
