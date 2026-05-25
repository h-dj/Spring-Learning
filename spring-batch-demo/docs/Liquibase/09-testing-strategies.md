# Chapter 9: Testing Strategies

## Overview
Comprehensive testing strategies for Liquibase migrations to ensure reliability and prevent issues.

## 9.1 Testing Overview

### Why Test Migrations?
- Catch errors early
- Validate rollback procedures
- Ensure idempotency
- Prevent data loss
- Verify constraints and indexes

### Testing Pyramid
```
        /\
       /E2E\          <- End-to-end tests
      /----\
     /Int.  \         <- Integration tests
    /--------\
   /Unit     \        <- Unit tests
  /------------\
```

## 9.2 Unit Testing Migrations

### Testing YAML Syntax
```java
class LiquibaseSyntaxTest {

    @Test
    void validateChangelogSyntax() throws Exception {
        // Load and parse changelog
        Resource[] resources = new PathMatchingResourcePatternResolver()
            .getResources("classpath:db/changelog/**/*.yaml");

        for (Resource resource : resources) {
            try (InputStream is = resource.getInputStream()) {
                // Parse YAML
                Yaml yaml = new Yaml();
                Map<String, Object> config = yaml.load(is);

                // Validate structure
                assertNotNull(config.get("databaseChangeLog"),
                    "Missing databaseChangeLog in " + resource.getFilename());

                Map<String, Object> changeLog =
                    (Map<String, Object>) config.get("databaseChangeLog");

                assertTrue(changeLog.containsKey("changeSet") ||
                    changeLog.containsKey("include"),
                    "Invalid changeLog structure in " + resource.getFilename());
            }
        }
    }

    @Test
    void validateChangeSetUniqueness() {
        Map<String, Set<String>> changeSetIds = new HashMap<>();

        // Load all changelogs
        Resource[] resources = new PathMatchingResourcePatternResolver()
            .getResources("classpath:db/changelog/**/*.yaml");

        for (Resource resource : resources) {
            // Parse and collect IDs
            // Assert uniqueness across all files
        }
    }
}
```

### Testing Custom Changes
```java
class CustomChangeTest {

    @Test
    void testCustomChangeValidation() {
        MyCustomChange change = new MyCustomChange();
        change.setTableName("test_table");
        change.setColumnName("test_column");

        ValidationErrors errors = change.validate(
            new H2Database());

        assertTrue(errors.hasErrors(),
            "Should have validation errors for missing required fields");
    }

    @Test
    void testCustomChangeExecution() throws Exception {
        MyCustomChange change = new MyCustomChange();
        change.setTableName("test_table");
        change.setColumnName("test_column");
        change.setUp();

        JdbcConnection connection = new JdbcConnection(
            DriverManager.getConnection("jdbc:h2:mem:test"));

        change.execute(connection.getDatabase());

        assertEquals("Custom change executed",
            change.getConfirmationMessage());
    }
}
```

### Mocking Dependencies
```java
class MockedChangeTest {

    @Test
    void testDataMigrationWithMocks() {
        // Mock database connection
        JdbcConnection mockConnection = mock(JdbcConnection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);

        // Setup expectations
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mock(PreparedStatement.class));

        // Execute
        DataMigrationChange change = new DataMigrationChange();
        change.setSourceTable("source");
        change.setTargetTable("target");

        change.execute(mockDatabase);

        // Verify
        verify(mockConnection).prepareStatement(
            contains("INSERT INTO target SELECT * FROM source"));
    }
}
```

## 9.3 Integration Testing

### H2 Database Testing
```java
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.liquibase.enabled=true",
    "spring.liquibase.change-log=classpath:db/changelog/test-master.yaml"
})
class LiquibaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testMigrationExecuted() {
        // Verify changelog table exists
        assertTrue(tableExists("DATABASECHANGELOG"));

        // Verify specific migration was applied
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM DATABASECHANGELOG " +
            "WHERE ID = 'create-users-table'",
            Integer.class);
        assertEquals(1, count);
    }

    @Test
    void testTableCreated() {
        assertTrue(tableExists("USERS"));
        assertTrue(columnExists("USERS", "ID"));
        assertTrue(columnExists("USERS", "USERNAME"));
        assertTrue(columnExists("USERS", "EMAIL"));
    }

    @Test
    void testConstraintsApplied() {
        // Check primary key
        assertTrue(hasPrimaryKey("USERS", "ID"));

        // Check unique constraints
        assertTrue(hasUniqueConstraint("USERS", "UK_USERS_EMAIL"));

        // Check foreign keys
        assertTrue(hasForeignKey("ORDERS", "FK_ORDERS_USER"));
    }

    private boolean tableExists(String tableName) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_name = ?",
            Integer.class, tableName.toUpperCase()) > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns " +
            "WHERE table_name = ? AND column_name = ?",
            Integer.class, tableName.toUpperCase(), columnName.toUpperCase()) > 0;
    }
}
```

### Test Container Testing
```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration-test")
class ContainerizedMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> true);
    }

    @Test
    void testMigrationOnPostgres() {
        // Tests run against real PostgreSQL
        assertTrue(tableExists("users"));
        assertTrue(columnExists("users", "id"));
    }
}
```

### Cross-Database Testing
```java
@ParameterizedTest
@EnumSource(value = DatabaseType.class,
    names = {"H2", "MYSQL", "POSTGRESQL"})
void testMigrationOnMultipleDatabases(DatabaseType type) {
    DataSource dataSource = createDataSourceFor(type);

    // Run migration
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:db/changelog/test-master.yaml");
    liquibase.update();

    // Verify
    assertTrue(tableExists(dataSource, "USERS"));
}
```

## 9.4 Rollback Testing

### Automated Rollback Tests
```java
@SpringBootTest
@ActiveProfiles("rollback-test")
@TestPropertySource(properties = {
    "spring.liquibase.enabled=true"
})
class RollbackTest {

    @Autowired
    private SpringLiquibase liquibase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testRollbackCapability() throws Exception {
        // Tag current state
        String tag = "test-before-rollback-" + System.currentTimeMillis();
        liquibase.tag(tag);

        // Apply test changes
        applyTestChanges();

        // Verify changes applied
        assertTrue(tableExists("test_table"));

        // Rollback
        liquibase.rollback(tag, "");

        // Verify rollback
        assertFalse(tableExists("test_table"));
    }

    private void applyTestChanges() {
        jdbcTemplate.execute(
            "CREATE TABLE test_table (id INT PRIMARY KEY)");
    }
}
```

### Rollback Verification
```java
class RollbackVerificationTest {

    @Test
    void verifyRollbackSql() throws Exception {
        // Generate rollback SQL without executing
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        Liquibase liquibase = new Liquibase(
            "classpath:db/changelog/master.yaml",
            new ClassLoaderResourceAccessor(),
            new JdbcConnection(
                DriverManager.getConnection("jdbc:h2:mem:test")));

        liquibase.rollback(1, "", printWriter);

        String rollbackSql = writer.toString();

        // Verify rollback SQL
        assertTrue(rollbackSql.contains("DROP TABLE"),
            "Rollback should contain DROP TABLE");
        assertFalse(rollbackSql.contains("CREATE TABLE"),
            "Rollback should not contain CREATE TABLE");
    }
}
```

## 9.5 Testing Data Changes

### Data Integrity Tests
```java
@SpringBootTest
class DataIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDataNotLostOnMigration() {
        // Insert test data before migration
        jdbcTemplate.execute(
            "INSERT INTO users (username, email) VALUES " +
            "('test1', 'test1@example.com'), " +
            "('test2', 'test2@example.com')");

        // Run migration
        // (assuming migration adds a column)
        runMigration();

        // Verify data preserved
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users",
            Integer.class);
        assertEquals(2, count);

        // Verify new column has expected values
        String email = jdbcTemplate.queryForObject(
            "SELECT email FROM users WHERE username = 'test1'",
            String.class);
        assertEquals("test1@example.com", email);
    }
}
```

### Reference Data Tests
```java
@SpringBootTest
class ReferenceDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testReferenceDataLoaded() {
        // Verify reference data migrations
        assertTrue(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM configuration",
            Integer.class) > 0);

        assertEquals("production",
            jdbcTemplate.queryForObject(
                "SELECT value FROM configuration WHERE key = 'environment'",
                String.class));
    }
}
```

## 9.6 Performance Testing

### Migration Time Tests
```java
class PerformanceTest {

    @Test
    void testMigrationCompletesWithinTimeout() throws Exception {
        DataSource dataSource = createTestDataSource();

        long startTime = System.currentTimeMillis();
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/large-migration.yaml");
        liquibase.update();
        long duration = System.currentTimeMillis() - startTime;

        // Assert migration completes within expected time
        assertTrue(duration < 60000,
            "Migration should complete within 60 seconds, took: " + duration);
    }

    @Test
    void testIndexCreationPerformance() {
        // Test index creation doesn't cause timeout
        // Measure and assert performance
    }
}
```

## 9.7 Testing Preconditions

### Precondition Validation
```java
class PreconditionTest {

    @Test
    void testPreconditionFailureHandling() {
        ChangeSet changeSet = new ChangeSet("1", "test", false, false,
            "test.yaml", null, null, null);

        PreconditionContainer preconditions = new PreconditionContainer();
        preconditions.addPrecondition(
            new TableExistsPrecondition("non_existent_table"));

        changeSet.setPreconditions(preconditions);

        // Should fail precondition check
        assertThrows(PrematureExecutionException.class,
            () -> changeSet.execute(
                new AbstractDatabase(null) {
                    // Mock database
                }));
    }
}
```

## 9.8 Continuous Integration Testing

### CI Pipeline Tests
```yaml
# .github/workflows/test.yml
name: Migration Tests

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: test
        ports: [5432:5432]
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3

      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Tests
        run: |
          mvn test \
            -Dspring.datasource.url=jdbc:postgresql://localhost:5432/test \
            -Dspring.datasource.username=postgres \
            -Dspring.datasource.password=test \
            -Dspring.liquibase.enabled=true
```

### Parallel Test Execution
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>classesAndMethods</parallel>
        <threadCount>4</threadCount>
        <useUnlimitedThreads>true</useUnlimitedThreads>
    </configuration>
</plugin>
```

## 9.9 Test Data Management

### Test Data Factory
```java
@Component
public class TestDataFactory {

    private final JdbcTemplate jdbcTemplate;

    public User createUser(String username, String email) {
        jdbcTemplate.execute(
            "INSERT INTO users (username, email) VALUES (?, ?)",
            username, email);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    public void cleanup() {
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM orders");
    }
}
```

### Test Containers for Isolation
```java
@TestConfiguration
static class TestContainersConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");
    }
}
```

## 9.10 Mutation Testing

### Pitest Configuration
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.14.0</version>
    <configuration>
        <targetClasses>
            <param>com.example.liquibase.*</param>
        </targetClasses>
        <targetTests>
            <param>com.example.liquibase.*Test</param>
        </targetTests>
    </configuration>
</plugin>
```

## 9.11 Contract Testing

### Consumer-Driven Contracts
```java
interface LiquibaseContract {
    @Given("a database with Liquibase installed")
    @When("a migration is executed")
    @Then("the changes are applied successfully")
    void migrationAppliesChanges();
}
```

## 9.12 Test Summary Checklist

```markdown
## Test Coverage Checklist

### Unit Tests
- [ ] YAML syntax validation
- [ ] ChangeSet ID uniqueness
- [ ] Custom change validation
- [ ] Custom change execution
- [ ] Precondition logic

### Integration Tests
- [ ] Migration execution on H2
- [ ] Migration execution on target database
- [ ] Table creation
- [ ] Column addition
- [ ] Constraint application
- [ ] Index creation
- [ ] Data preservation

### Rollback Tests
- [ ] Rollback execution
- [ ] Rollback verification
- [ ] Rollback SQL generation

### Performance Tests
- [ ] Migration completion time
- [ ] Large dataset handling
- [ ] Memory usage

### Security Tests
- [ ] Sensitive data handling
- [ ] Connection security
```

## Summary

| Test Type | Purpose | Example |
|-----------|---------|---------|
| Unit | Syntax, validation | YAML parsing, ChangeSet ID |
| Integration | End-to-end execution | H2, Testcontainers |
| Rollback | Verify rollback works | Tag-based rollback |
| Performance | Measure execution time | Large migrations |
| Security | Data protection | Sensitive data |

## Next Steps
- [Chapter 10: CI/CD Integration](10-ci-cd-integration.md)
- Automate migrations in pipeline
- Deployment strategies
