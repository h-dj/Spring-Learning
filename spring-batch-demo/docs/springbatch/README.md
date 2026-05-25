# Spring Batch Learning Guide

A comprehensive step-by-step learning path for mastering Spring Batch batch processing.

## Overview

Spring Batch is a lightweight, comprehensive batch framework designed to enable the development of robust batch applications. It provides reusable functions for processing large volumes of records, including logging, transaction management, job statistics, and restart capabilities.

## Learning Path

| Chapter | Topic | Description | Est. Time |
|---------|-------|-------------|-----------|
| [01-quick-start](01-quick-start.md) | Quick Start | Setup and first batch job | 30 min |
| [02-core-concepts](02-core-concepts.md) | Core Concepts | Job, Step, JobRepository | 1 hour |
| [03-job-configuration](03-job-configuration.md) | Job Configuration | Job parameters, execution | 1 hour |
| [04-step-types](04-step-types.md) | Step Types | Tasklet, Chunk-oriented | 1.5 hours |
| [05-item-reader](05-item-reader.md) | Item Readers | JDBC, Mongo, File readers | 2 hours |
| [06-item-writer](06-item-writer.md) | Item Writers | Database, File writers | 2 hours |
| [07-item-processor](07-item-processor.md) | Item Processors | Data transformation | 1 hour |
| [08-listeners](08-listeners.md) | Listeners | Job/Step listeners | 1 hour |
| [09-testing](09-testing-strategies.md) | Testing Strategies | Unit & integration tests | 2 hours |
| [10-scheduling](10-scheduling.md) | Scheduling | Cron jobs, triggers | 1.5 hours |
| [11-error-handling](11-error-handling.md) | Error Handling | Retry, skip, restart | 2 hours |
| [12-advanced-features](12-advanced-features.md) | Advanced Features | Partitioning, parallel | 2 hours |
| [13-multithreaded-processing](13-multithreaded-processing.md) | Multi-threaded Processing | Multi-threaded Step, AsyncProcessor, Partitioning internals | 2 hours |
| [14-multifile-concurrency-analysis](14-multifile-concurrency-analysis.md) | Production Analysis | Multi-file concurrency, CPU troubleshooting, SimpleAsyncTaskExecutor | 1 hour |

## Quick Start

### 1. Add Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Create Your First Job
```java
@Configuration
public class FirstJobConfig {

    @Bean
    public Job firstJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("firstJob", jobRepository)
            .start(step1)
            .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Hello Spring Batch!");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

## Project Structure

```
.claude/docs/springbatch/
├── README.md                    # This file
├── 01-quick-start.md           # Getting started
├── 02-core-concepts.md         # Fundamentals
├── 03-job-configuration.md     # Job configuration
├── 04-step-types.md            # Step types
├── 05-item-reader.md           # ItemReader implementations
├── 06-item-writer.md           # ItemWriter implementations
├── 07-item-processor.md        # ItemProcessor
├── 08-listeners.md             # Listeners
├── 09-testing-strategies.md    # Testing
├── 10-scheduling.md            # Scheduling
├── 11-error-handling.md        # Error handling
├── 12-advanced-features.md     # Advanced features
├── 13-multithreaded-processing.md  # Multi-threaded processing
└── 14-multifile-concurrency-analysis.md  # Multi-file concurrency analysis

src/main/java/cn/reid/springbatchdemo/
├── SpringBatchDemoApplication.java
├── job/                        # Job configurations
├── reader/                     # ItemReaders
├── writer/                     # ItemWriters
├── processor/                  # ItemProcessors
├── listener/                   # Listeners
└── service/                    # Services
```

## Common Commands

```bash
# Run application
mvn spring-boot:run

# Run with specific job
mvn spring-boot:run -Dspring.batch.job.names=firstJob

# List all jobs
curl http://localhost:8080/api/jobs

# Launch job
curl -X POST http://localhost:8080/api/jobs/firstJob
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    JobLauncher                           │
│  (triggers job execution with parameters)                │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│                      Job                                 │
│  (named sequence of Steps)                               │
└────────────────┬────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
┌───────────────┐  ┌───────────────┐
│     Step 1    │  │     Step 2    │
│ (Tasklet/Chunk)│  │ (Tasklet/Chunk)│
└───────┬───────┘  └───────┬───────┘
        │                 │
        ▼                 ▼
┌───────────────┐  ┌───────────────┐
│   ItemReader  │  │   ItemWriter  │
│   ItemProcess │  │   ItemProcess │
└───────────────┘  └───────────────┘
        │                 │
        └────────┬────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│                  JobRepository                            │
│  (persists job metadata and execution history)           │
└─────────────────────────────────────────────────────────┘
```

## Key Concepts

### Job
A batch job is the container that encapsulates the entire batch process. It consists of one or more steps.

### Step
A step is a domain object that encapsulates an independent, sequential phase of a batch job.

### Item
An item is a singular piece of data processed by a batch job (e.g., a row from a database).

### Chunk
A chunk is a collection of items processed together in a single transaction.

### JobRepository
Stores metadata about jobs and their executions (Spring Batch metadata tables).

### JobLauncher
Launches jobs with parameters and manages execution.

## Spring Batch Metadata Tables

| Table | Description |
|-------|-------------|
| BATCH_JOB_INSTANCE | Each distinct job instance |
| BATCH_JOB_EXECUTION | Each execution of a job |
| BATCH_STEP_EXECUTION | Each execution of a step |
| BATCH_JOB_EXECUTION_CONTEXT | Job-level context |
| BATCH_STEP_EXECUTION_CONTEXT | Step-level context |

## Prerequisites

- Java 17+
- Maven 3.6+
- Basic Spring Boot knowledge
- SQL fundamentals

## Estimated Completion

**Total: ~15-17 hours** for complete learning path

## Progress Tracking

- [ ] Chapter 1: Quick Start
- [ ] Chapter 2: Core Concepts
- [ ] Chapter 3: Job Configuration
- [ ] Chapter 4: Step Types
- [ ] Chapter 5: Item Readers
- [ ] Chapter 6: Item Writers
- [ ] Chapter 7: Item Processors
- [ ] Chapter 8: Listeners
- [ ] Chapter 9: Testing Strategies
- [ ] Chapter 10: Scheduling
- [ ] Chapter 11: Error Handling
- [ ] Chapter 12: Advanced Features
- [ ] Chapter 13: Multi-threaded Processing
- [ ] Chapter 14: Multi-file Concurrency Analysis

## Resources

- [Official Documentation](https://docs.spring.io/spring-batch/)
- [API Reference](https://docs.spring.io/spring-batch/docs/current/api/)
- [Spring Batch Guide](https://spring.io/guides/gs/batch-processing/)
- [GitHub Repository](https://github.com/spring-projects/spring-batch)

---

*Part of Spring Batch Demo learning materials*
