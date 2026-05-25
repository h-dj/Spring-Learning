# Chapter 10: Scheduling

## Overview
Learn how to schedule Spring Batch jobs using Spring's scheduling capabilities and cron expressions.

## 10.1 Scheduling Overview

### Scheduling Options
- **@Scheduled**: Simple cron-based scheduling
- **@EnableScheduling**: Enable scheduling feature
- **Quartz**: Enterprise scheduling
- **TaskScheduler**: Programmatic scheduling

## 10.2 Enable Scheduling

### Main Configuration
```java
@SpringBootApplication
@EnableScheduling
public class SpringBatchDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchDemoApplication.class, args);
    }
}
```

### Application Properties
```yaml
spring:
  task:
    scheduling:
      pool:
        size: 5
    execution:
      pool:
        max-size: 10
```

## 10.3 Basic Scheduling

### @Scheduled Annotation
```java
@Component
public class ScheduledJobLauncher {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job dailyReportJob;

    // Every day at 2:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void launchDailyReportJob() {
        try {
            System.out.println("Starting scheduled daily report job...");
            JobParameters params = new JobParametersBuilder()
                .addDate("run.date", new Date())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            JobExecution execution = jobLauncher.run(dailyReportJob, params);
            System.out.println("Job status: " + execution.getStatus());
        } catch (Exception e) {
            System.err.println("Failed to start scheduled job: " + e.getMessage());
        }
    }
}
```

### Multiple Schedules
```java
@Component
public class MultiScheduledJobLauncher {

    @Autowired
    private Job dailyJob;

    @Autowired
    private Job hourlyJob;

    @Autowired
    private Job weeklyJob;

    // Every hour
    @Scheduled(cron = "0 0 * * * ?")
    public void launchHourlyJob() {
        launchJob(hourlyJob, "hourly");
    }

    // Every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void launchDailyJob() {
        launchJob(dailyJob, "daily");
    }

    // Every Sunday at 3:00 AM
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void launchWeeklyJob() {
        launchJob(weeklyJob, "weekly");
    }

    private void launchJob(Job job, String type) {
        try {
            JobParameters params = new JobParametersBuilder()
                .addString("schedule.type", type)
                .addDate("run.date", new Date())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(job, params);
        } catch (Exception e) {
            System.err.println("Scheduled job failed: " + e.getMessage());
        }
    }
}
```

## 10.4 Cron Expressions

### Format
```
second minute hour day-of-month month day-of-week
```

### Examples
```java
// Every 5 minutes
@Scheduled(cron = "0 */5 * * * ?")

// Every day at 2:30 AM
@Scheduled(cron = "0 30 2 * * ?")

// Every Monday at 9:00 AM
@Scheduled(cron = "0 0 9 ? * MON")

// Last day of month at midnight
@Scheduled(cron = "0 0 0 L * ?")

// Every 15 seconds (for testing)
@Scheduled(cron = "0/15 * * * * ?")
```

### Cron Ranges
```java
// Every hour between 9 AM and 5 PM
@Scheduled(cron = "0 0 9-17 * * ?")

// January to June
@Scheduled(cron = "0 0 0 1 1-6 ?")

// Monday to Friday
@Scheduled(cron = "0 0 0 ? * MON-FRI")
```

## 10.5 Dynamic Scheduling

### Programmatic Scheduling
```java
@Configuration
@EnableScheduling
public class DynamicSchedulingConfig {

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private JobLauncher jobLauncher;

    private final Map<String, ScheduledFuture<?>> scheduledJobs =
        new ConcurrentHashMap<>();

    public void scheduleJob(String jobName, Job job, String cronExpression) {
        // Cancel existing if present
        cancelJob(jobName);

        // Create task
        Runnable task = () -> {
            try {
                JobParameters params = new JobParametersBuilder()
                    .addString("triggered.by", "scheduler")
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

                jobLauncher.run(job, params);
            } catch (Exception e) {
                System.err.println("Scheduled job failed: " + e.getMessage());
            }
        };

        // Schedule using cron trigger
        CronTrigger trigger = new CronTrigger(cronExpression);
        ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);

        scheduledJobs.put(jobName, future);
        System.out.println("Scheduled job: " + jobName + " with cron: " + cronExpression);
    }

    public void cancelJob(String jobName) {
        ScheduledFuture<?> future = scheduledJobs.get(jobName);
        if (future != null) {
            future.cancel(false);
            scheduledJobs.remove(jobName);
            System.out.println("Cancelled job: " + jobName);
        }
    }
}
```

### REST API for Scheduling
```java
@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    @Autowired
    private DynamicSchedulingConfig scheduler;

    @Autowired
    private JobRegistry jobRegistry;

    @PostMapping("/schedule/{jobName}")
    public ResponseEntity<String> scheduleJob(
            @PathVariable String jobName,
            @RequestParam String cronExpression) {

        try {
            Job job = jobRegistry.getJob(jobName);
            scheduler.scheduleJob(jobName, job, cronExpression);
            return ResponseEntity.ok("Scheduled: " + jobName);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/cancel/{jobName}")
    public ResponseEntity<String> cancelJob(@PathVariable String jobName) {
        scheduler.cancelJob(jobName);
        return ResponseEntity.ok("Cancelled: " + jobName);
    }
}
```

## 10.6 Quartz Scheduler

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### Quartz Configuration
```java
@Configuration
public class QuartzConfig {

    @Autowired
    private JobLauncher jobLauncher;

    @Bean
    public JobDetail batchJobDetail() {
        return JobBuilder.newJob(BatchJobLauncher.class)
            .withIdentity("batchJob", "batchJobs")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger batchJobTrigger() {
        return TriggerBuilder.newTrigger()
            .forIdentity(batchJobDetail())
            .withSchedule(CronScheduleBuilder
                .dailyAtHourAndMinute(2, 0))
            .build();
    }
}
```

### Quartz Job
```java
public class BatchJobLauncher extends QuartzJobBean {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRegistry jobRegistry;

    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {

        String jobName = context.getMergedJobDataMap()
            .getString("jobName");

        try {
            Job job = jobRegistry.getJob(jobName);
            JobParameters params = new JobParametersBuilder()
                .addString("triggered.by", "quartz")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(job, params);
        } catch (Exception e) {
            throw new JobExecutionException(
                "Failed to execute batch job: " + jobName, e);
        }
    }
}
```

## 10.7 Scheduled Job Management

### Job Scheduler Service
```java
@Service
public class ScheduledJobService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private JobRepository jobRepository;

    private final Map<String, ScheduledFuture<?>> scheduledJobs =
        new ConcurrentHashMap<>();

    @Autowired
    private TaskScheduler taskScheduler;

    public void schedule(String jobName, String cronExpression) {
        try {
            Job job = jobRegistry.getJob(jobName);

            Runnable task = createJobTask(job);
            CronTrigger trigger = new CronTrigger(cronExpression);

            ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);
            scheduledJobs.put(jobName, future);

            // Store schedule info
            storeScheduleInfo(jobName, cronExpression);

        } catch (Exception e) {
            throw new RuntimeException("Failed to schedule job: " + jobName, e);
        }
    }

    public void unschedule(String jobName) {
        ScheduledFuture<?> future = scheduledJobs.remove(jobName);
        if (future != null) {
            future.cancel(false);
        }
        removeScheduleInfo(jobName);
    }

    public List<ScheduledJobInfo> getScheduledJobs() {
        List<ScheduledJobInfo> jobs = new ArrayList<>();
        for (String jobName : scheduledJobs.keySet()) {
            jobs.add(new ScheduledJobInfo(jobName, getCronExpression(jobName)));
        }
        return jobs;
    }

    private Runnable createJobTask(Job job) {
        return () -> {
            try {
                JobParameters params = new JobParametersBuilder()
                    .addString("triggered.by", "scheduler")
                    .addDate("run.date", new Date())
                    .addLong("run.time", System.currentTimeMillis())
                    .toJobParameters();

                jobLauncher.run(job, params);
            } catch (Exception e) {
                System.err.println("Scheduled job failed: " + e.getMessage());
            }
        };
    }

    private void storeScheduleInfo(String jobName, String cronExpression) {
        // Store in database or properties
    }

    private void removeScheduleInfo(String jobName) {
        // Remove from storage
    }

    private String getCronExpression(String jobName) {
        // Retrieve from storage
        return null;
    }
}
```

### Scheduled Job Info DTO
```java
public class ScheduledJobInfo {
    private String jobName;
    private String cronExpression;
    private Date nextExecution;
    private boolean running;

    // Getters and setters
}
```

## 10.8 Monitoring Scheduled Jobs

### Scheduled Tasks
```java
@RestController
@RequestMapping("/api/scheduled")
public class ScheduledJobController {

    @Autowired
    private ScheduledJobService scheduledJobService;

    @GetMapping
    public List<ScheduledJobInfo> getScheduledJobs() {
        return scheduledJobService.getScheduledJobs();
    }

    @PostMapping("/{jobName}")
    public ResponseEntity<String> schedule(
            @PathVariable String jobName,
            @RequestParam String cronExpression) {
        scheduledJobService.schedule(jobName, cronExpression);
        return ResponseEntity.ok("Scheduled");
    }

    @DeleteMapping("/{jobName}")
    public ResponseEntity<String> unschedule(@PathVariable String jobName) {
        scheduledJobService.unschedule(jobName);
        return ResponseEntity.ok("Unscheduled");
    }
}
```

### Health Check
```java
@Component
public class ScheduledJobHealthIndicator extends HealthIndicator {

    @Autowired
    private ScheduledJobService scheduledJobService;

    @Override
    protected Health health() {
        List<ScheduledJobInfo> jobs =
            scheduledJobService.getScheduledJobs();

        if (jobs.isEmpty()) {
            return Health.up()
                .withDetail("scheduledJobs", 0)
                .build();
        }

        return Health.up()
            .withDetail("scheduledJobs", jobs.size())
            .build();
    }
}
```

## 10.9 Distributed Scheduling

### Preventing Duplicate Execution
```java
@Component
public class DistributedScheduledJob {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private DistributedLock lockService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyJob() {
        String lockKey = "daily-job-lock";

        try {
            if (lockService.tryLock(lockKey, Duration.ofMinutes(30))) {
                try {
                    executeJob();
                } finally {
                    lockService.unlock(lockKey);
                }
            } else {
                System.out.println("Daily job already running on another node");
            }
        } catch (Exception e) {
            System.err.println("Daily job failed: " + e.getMessage());
        }
    }

    private void executeJob() throws Exception {
        // Execute job
    }
}
```

### Redis-based Lock
```java
@Component
public class RedisDistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean tryLock(String key, Duration duration) {
        String value = UUID.randomUUID().toString();
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(key, value, duration);
        return Boolean.TRUE.equals(result);
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }
}
```

## 10.10 Scheduling Best Practices

### Idempotent Jobs
```java
@Component
public class IdempotentScheduledJob {

    @Scheduled(cron = "0 0 2 * * ?")
    public void runIdempotentJob() {
        // Check if already ran today
        if (hasRunToday()) {
            System.out.println("Job already ran today, skipping");
            return;
        }

        // Execute job
        executeJob();

        // Mark as ran
        markAsRanToday();
    }
}
```

### Error Handling
```java
@Component
public class RobustScheduledJob {

    @Scheduled(cron = "0 0 2 * * ?")
    public void runRobustJob() {
        try {
            executeJob();
        } catch (Exception e) {
            // Log error
            // Send alert
            // Retry if needed
        }
    }

    private void executeJob() {
        // Job logic
    }
}
```

### Resource Management
```java
@Configuration
public class SchedulingConfig {

    @Bean
    @Primary
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler =
            new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("Scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }
}
```

## 10.11 Practice Scenario

### Scenario: Scheduled Report Generation System
```java
@Configuration
public class ReportSchedulingConfig {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private TaskScheduler taskScheduler;

    @PostConstruct
    public void scheduleReports() {
        // Daily report at 6:00 AM
        scheduleJob("dailySalesReport", "0 0 6 * * ?");

        // Weekly report on Monday at 8:00 AM
        scheduleJob("weeklySalesReport", "0 0 8 ? * MON");

        // Monthly report on 1st at midnight
        scheduleJob("monthlySalesReport", "0 0 0 1 * ?");
    }

    private void scheduleJob(String jobName, String cronExpression) {
        try {
            Job job = jobRegistry.getJob(jobName);

            Runnable task = () -> {
                try {
                    JobParameters params = new JobParametersBuilder()
                        .addString("triggered.by", "scheduler")
                        .addDate("run.date", new Date())
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters();

                    jobLauncher.run(job, params);
                } catch (Exception e) {
                    System.err.println("Scheduled job failed: " + jobName);
                    sendAlert(jobName, e);
                }
            };

            CronTrigger trigger = new CronTrigger(cronExpression);
            taskScheduler.schedule(task, trigger);

            System.out.println("Scheduled " + jobName + " with: " + cronExpression);
        } catch (Exception e) {
            System.err.println("Failed to schedule " + jobName + ": " + e.getMessage());
        }
    }

    private void sendAlert(String jobName, Exception e) {
        // Send alert to monitoring system
    }
}
```

## 10.12 Summary

| Feature | Description | Example |
|---------|-------------|---------|
| @Scheduled | Simple cron scheduling | `@Scheduled(cron = "0 0 2 * * ?")` |
| TaskScheduler | Programmatic scheduling | `taskScheduler.schedule(task, trigger)` |
| CronTrigger | Trigger with cron | `new CronTrigger(cronExpression)` |
| Quartz | Enterprise scheduling | `QuartzJobBean` |
| Distributed Lock | Prevent duplicates | Redis-based lock |

## 10.13 Next Steps

- [Chapter 11: Error Handling](11-error-handling.md)
- Learn retry, skip, and restart mechanisms
- Handle failures gracefully

## Exercises

### Exercise 1: Basic Scheduling
Create a scheduled job that:
1. Runs every minute
2. Logs execution time
3. Uses cron expression

### Exercise 2: Dynamic Scheduling
Create a REST API that:
1. Schedules jobs with cron expressions
2. Lists scheduled jobs
3. Cancels scheduled jobs

### Exercise 3: Distributed Scheduling
Implement distributed lock to:
1. Prevent duplicate execution
2. Use Redis for lock storage
3. Handle lock release on failure

---
*Duration: 1.5 hours*
