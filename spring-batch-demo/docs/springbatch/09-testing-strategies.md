# Chapter 9: Testing Strategies

## Overview
Comprehensive testing strategies for Spring Batch jobs, steps, and components.

## 9.1 Testing Overview

### Test Types
- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test components together
- **End-to-End Tests**: Test complete job execution

### Test Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## 9.2 Unit Testing Tasklets

```java
@SpringBootTest
@ActiveProfiles("test")
class TaskletTest {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void testTaskletExecution() throws Exception {
        // Create tasklet
        Tasklet tasklet = (contribution, chunkContext) -> {
            chunkContext.getStepContext()
                .getStepExecution()
                .getExecutionContext()
                .put("processed", true);
            return RepeatStatus.FINISHED;
        };

        // Create step
        Step step = new StepBuilder("testStep", jobRepository())
            .tasklet(tasklet, transactionManager)
            .build();

        // Execute step
        JobExecution jobExecution = jobLauncher().run(
            new JobBuilder("testJob", jobRepository())
                .start(step)
                .build(),
            new JobParameters());

        // Verify
        assertEquals(BatchStatus.COMPLETED,
            jobExecution.getStatus());
        assertEquals(1, jobExecution.getStepExecutions().size());
    }
}
```

### Mocking Tasklet
```java
@ExtendWith(MockitoExtension.class)
class MockedTaskletTest {

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepContext stepContext;

    @Mock
    private ExecutionContext executionContext;

    private Tasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new CustomTasklet();
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getStep(
            new StepExecution("testStep", newExecution()).thenReturn JobExecution(1L)));
        when(stepContext.getStepExecution().getExecutionContext())
            .thenReturn(executionContext);
    }

    @Test
    void testTasklet() throws Exception {
        RepeatStatus status = tasklet.execute(null, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(executionContext).put("key", "value");
    }
}
```

## 9.3 Testing ItemProcessors

```java
@SpringBootTest
class ProcessorTest {

    @Test
    void testTransformationProcessor() {
        ItemProcessor<Customer, Customer> processor =
            new TransformationProcessor();

        Customer input = new Customer();
        input.setFirstName("JOHN");
        input.setLastName("DOE");
        input.setEmail("JOHN@EXAMPLE.COM");

        Customer output = processor.process(input);

        assertEquals("John", output.getFirstName());
        assertEquals("Doe", output.getLastName());
        assertEquals("john@example.com", output.getEmail());
    }

    @Test
    void testFilterProcessor() {
        ItemProcessor<Customer, Customer> processor =
            new FilterProcessor();

        Customer inactive = new Customer();
        inactive.setStatus("INACTIVE");

        Customer result = processor.process(inactive);

        assertNull(result);  // Filtered out
    }

    @Test
    void testValidationProcessor() {
        ItemProcessor<Customer, Customer> processor =
            new ValidationProcessor();

        Customer invalid = new Customer();
        invalid.setEmail("invalid-email");

        assertThrows(ValidationException.class, () ->
            processor.process(invalid));
    }
}
```

## 9.4 Testing ItemReaders

```java
@SpringBootTest
class ReaderTest {

    @Test
    void testListReader() {
        List<String> data = Arrays.asList("a", "b", "c", "d", "e");
        ItemReader<String> reader = new ListItemReader<>(data);

        assertEquals("a", reader.read());
        assertEquals("b", reader.read());
        assertEquals("c", reader.read());
        assertNull(reader.read());  // End of data
    }

    @Test
    void testJdbcReader() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(
            createTestDataSource());

        jdbcTemplate.execute(
            "CREATE TABLE test (id INT, value VARCHAR(50))");
        jdbcTemplate.execute(
            "INSERT INTO test VALUES (1, 'one'), (2, 'two')");

        JdbcCursorItemReader<TestEntity> reader =
            new JdbcCursorItemReaderBuilder<TestEntity>()
                .dataSource(createTestDataSource())
                .sql("SELECT * FROM test")
                .rowMapper((rs, rowNum) ->
                    new TestEntity(rs.getInt("id"),
                        rs.getString("value")))
                .build();

        assertNotNull(reader.read());
        assertNotNull(reader.read());
        assertNull(reader.read());
    }
}
```

## 9.5 Testing ItemWriters

```java
@SpringBootTest
class WriterTest {

    @Test
    void testListWriter() throws Exception {
        List<String> data = new ArrayList<>();
        ItemWriter<String> writer = data::add;

        writer.write(Arrays.asList("a", "b", "c"));

        assertEquals(3, data.size());
        assertEquals("a", data.get(0));
    }

    @Test
    void testJdbcWriter() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(
            createTestDataSource());

        jdbcTemplate.execute(
            "CREATE TABLE test (id INT, value VARCHAR(50))");

        JdbcBatchItemWriter<TestEntity> writer =
            new JdbcBatchItemWriterBuilder<TestEntity>()
                .dataSource(createTestDataSource())
                .sql("INSERT INTO test VALUES (?, ?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setInt(1, item.getId());
                    ps.setString(2, item.getValue());
                })
                .build();

        writer.write(Arrays.asList(
            new TestEntity(1, "one"),
            new TestEntity(2, "two")
        ));

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test", Integer.class);
        assertEquals(2, count);
    }
}
```

## 9.6 Integration Testing Jobs

```java
@SpringBootTest
@ActiveProfiles("test")
class JobIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    void testSimpleJob() throws Exception {
        Job job = new JobBuilder("testJob", jobRepository)
            .start(new StepBuilder("testStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Test executed");
                    return RepeatStatus.FINISHED;
                }, new DataSourceTransactionManager(dataSource))
                .build())
            .build();

        JobExecution execution = jobLauncher.run(job, new JobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void testJobWithParameters() throws Exception {
        Job job = new JobBuilder("parameterizedJob", jobRepository)
            .start(new StepBuilder("paramStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String param = chunkContext.getStepContext()
                        .getJobParameters().getString("param");
                    assertEquals("test-value", param);
                    return RepeatStatus.FINISHED;
                }, new DataSourceTransactionManager(dataSource))
                .build())
            .build();

        JobParameters params = new JobParametersBuilder()
            .addString("param", "test-value")
            .toJobParameters();

        JobExecution execution = jobLauncher.run(job, params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
```

### Test Configuration
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
  batch:
    jdbc:
      initialize-schema: always
      table-prefix: BATCH_
    job:
      enabled: false
```

## 9.7 Testing Chunk Processing

```java
@SpringBootTest
class ChunkProcessingTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void testChunkStep() throws Exception {
        List<String> testData = Arrays.asList(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        Job job = new JobBuilder("chunkTestJob", jobRepository)
            .start(new StepBuilder("chunkStep", jobRepository)
                .<String, String>chunk(3,
                    new DataSourceTransactionManager(dataSource))
                .reader(new ListItemReader<>(testData))
                .processor(item -> item.toUpperCase())
                .writer(items -> {
                    // Verify chunk size
                    assertEquals(3, items.size());
                })
                .build())
            .build();

        JobExecution execution = jobLauncher.run(job, new JobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        StepExecution stepExecution = execution.getStepExecutions().iterator().next();
        assertEquals(10, stepExecution.getReadCount());
        assertEquals(10, stepExecution.getWriteCount());
    }
}
```

## 9.8 Testing Listeners

```java
@SpringBootTest
class ListenerTest {

    @Test
    void testJobListener() throws Exception {
        List<String> events = Collections.synchronizedList(
            new ArrayList<>());

        JobExecutionListener listener = new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                events.add("beforeJob");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                events.add("afterJob");
            }
        };

        Job job = new JobBuilder("listenerJob", jobRepository)
            .listener(listener)
            .start(new StepBuilder("testStep", jobRepository)
                .tasklet((contribution, chunkContext) ->
                    RepeatStatus.FINISHED,
                    new DataSourceTransactionManager(dataSource))
                .build())
            .build();

        jobLauncher.run(job, new JobParameters());

        assertTrue(events.contains("beforeJob"));
        assertTrue(events.contains("afterJob"));
    }

    @Test
    void testStepListener() throws Exception {
        List<String> events = Collections.synchronizedList(
            new ArrayList<>());

        StepExecutionListener listener = new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                events.add("beforeStep:" + stepExecution.getStepName());
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                events.add("afterStep:" + stepExecution.getStepName());
                return stepExecution.getExitStatus();
            }
        };

        Job job = new JobBuilder("stepListenerJob", jobRepository)
            .start(new StepBuilder("testStep", jobRepository)
                .listener(listener)
                .tasklet((contribution, chunkContext) ->
                    RepeatStatus.FINISHED,
                    new DataSourceTransactionManager(dataSource))
                .build())
            .build();

        jobLauncher.run(job, new JobParameters());

        assertTrue(events.contains("beforeStep:testStep"));
        assertTrue(events.contains("afterStep:testStep"));
    }
}
```

## 9.9 Testing with H2

```java
@TestConfiguration
class TestDatabaseConfig {

    @Bean
    @Primary
    public DataSource testDataSource() {
        return DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
            .username("sa")
            .password("")
            .build();
    }
}

@SpringBootTest(classes = {TestDatabaseConfig.class})
@ActiveProfiles("test")
class H2DatabaseTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseOperations() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        jdbc.execute("CREATE TABLE test (id INT, value VARCHAR(50))");
        jdbc.execute("INSERT INTO test VALUES (1, 'test')");

        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM test", Integer.class);

        assertEquals(1, count);
    }
}
```

## 9.10 Testing with Testcontainers

```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration")
class IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testWithPostgres() {
        // Tests run against real PostgreSQL
        assertTrue(postgres.isRunning());
    }
}
```

## 9.11 Testing Job Execution

```java
@SpringBootTest
class JobExecutionTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void testJobExecution() throws Exception {
        Job job = new JobBuilder("executionTestJob", jobRepository)
            .start(new StepBuilder("executionTestStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // Simulate work
                    return RepeatStatus.FINISHED;
                }, new DataSourceTransactionManager(dataSource))
                .build())
            .build();

        JobExecution execution = jobLauncher.run(job, new JobParameters());

        // Verify job execution
        assertNotNull(execution);
        assertNotNull(execution.getJobInstance());
        assertNotNull(execution.getExecutionContext());

        // Verify step execution
        StepExecution stepExecution =
            execution.getStepExecutions().iterator().next();
        assertNotNull(stepExecution);
        assertEquals("executionTestStep", stepExecution.getStepName());
    }

    @Test
    void testJobRestart() throws Exception {
        AtomicInteger executionCount = new AtomicInteger(0);

        Job job = new JobBuilder("restartableJob", jobRepository)
            .start(new StepBuilder("restartableStep", jobRepository)
                .allowStartIfComplete(true)  // Allow restart
                .tasklet((contribution, chunkContext) -> {
                    executionCount.incrementAndGet();
                    return RepeatStatus.FINISHED;
                }, new DataSourceTransactionManager(dataSource))
                .build())
            .build();

        // First execution
        jobLauncher.run(job, new JobParameters());
        assertEquals(1, executionCount.get());

        // Second execution (should run again due to allowStartIfComplete)
        jobLauncher.run(job, new JobParameters());
        assertEquals(2, executionCount.get());
    }
}
```

## 9.12 Testing Error Handling

```java
@SpringBootTest
class ErrorHandlingTest {

    @Test
    void testJobFailure() throws Exception {
        Job job = new JobBuilder("failingJob", jobRepository)
            .start(new StepBuilder("failingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    throw new RuntimeException("Intentional failure");
                }, new DataSourceTransactionManager(dataSource))
                .build())
            .build();

        JobExecution execution = jobLauncher.run(job, new JobParameters());

        assertEquals(BatchStatus.FAILED, execution.getStatus());
        assertEquals(1, execution.getAllFailureExceptions().size());
        assertTrue(execution.getAllFailureExceptions().get(0)
            .getMessage().contains("Intentional failure"));
    }

    @Test
    void testSkipError() throws Exception {
        List<String> data = Arrays.asList("a", "b", "ERROR", "d");

        Job job = new JobBuilder("skipJob", jobRepository)
            .start(new StepBuilder("skipStep", jobRepository)
                .<String, String>chunk(2,
                    new DataSourceTransactionManager(dataSource))
                .reader(new ListItemReader<>(data))
                .processor(item -> {
                    if ("ERROR".equals(item)) {
                        throw new RuntimeException("Skip this item");
                    }
                    return item;
                })
                .writer(items -> {})
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(1)
                .build())
            .build();

        JobExecution execution = jobLauncher.run(job, new JobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
```

## 9.13 MockMvc Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobLauncher jobLauncher;

    @Test
    void testLaunchJob() throws Exception {
        mockMvc.perform(post("/api/jobs/launch")
                .param("jobName", "testJob")
                .param("param", "value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("STARTED"));
    }

    @Test
    void testGetJobs() throws Exception {
        mockMvc.perform(get("/api/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

## 9.14 Practice Scenario

### Scenario: Complete Job Test Suite
```java
@SpringBootTest
@ActiveProfiles("test")
class OrderProcessingJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    void testCompleteOrderProcessing() throws Exception {
        // Setup test data
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE orders (id INT, status VARCHAR(20))");
        jdbc.execute("INSERT INTO orders VALUES (1, 'PENDING')");
        jdbc.execute("INSERT INTO orders VALUES (2, 'PENDING')");
        jdbc.execute("INSERT INTO orders VALUES (3, 'PENDING')");

        // Create job
        Job job = new OrderProcessingJobConfig()
            .orderProcessingJob(jobRepository,
                createProcessingStep(jobRepository));

        // Execute
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncher.run(job, params);

        // Verify
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        StepExecution stepExecution = execution.getStepExecutions()
            .iterator().next();
        assertEquals(3, stepExecution.getReadCount());
        assertEquals(3, stepExecution.getWriteCount());

        // Verify data changes
        Integer processed = jdbc.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE status = 'PROCESSED'",
            Integer.class);
        assertEquals(3, processed);
    }
}
```

## 9.15 Summary

| Test Type | Purpose | Tools |
|-----------|---------|-------|
| Unit Test | Individual components | JUnit, Mockito |
| Integration Test | Component interaction | Spring Boot Test, H2 |
| End-to-End Test | Complete flow | Testcontainers |
| Performance Test | Load testing | JMH, Gatling |

## 9.16 Next Steps

- [Chapter 10: Scheduling](10-scheduling.md)
- Learn about cron jobs and triggers
- Schedule batch jobs

## Exercises

### Exercise 1: Unit Test Tasklet
Create unit tests for:
1. Tasklet with parameters
2. Tasklet with ExecutionContext
3. Tasklet with error handling

### Exercise 2: Integration Test
Create integration tests for:
1. Complete job execution
2. Job with multiple steps
3. Chunk processing with readers/writers

### Exercise 3: Error Test
Create error handling tests for:
1. Job failure
2. Skip configuration
3. Retry configuration

---
*Duration: 2 hours*
