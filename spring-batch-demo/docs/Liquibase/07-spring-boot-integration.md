# Chapter 7: Spring Boot Integration

## Overview
Master Spring Boot integration with Liquibase including auto-configuration, customization, and best practices.

## 7.1 Spring Boot Auto-configuration

### How It Works
When `spring-boot-starter-liquibase` is present, Spring Boot automatically:
1. Creates `SpringLiquibase` bean
2. Reads configuration from `application.yml/properties`
3. Executes migrations on startup
4. Manages lifecycle and dependencies

### Default Configuration
```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
    drop-first: false
    default-schema: public
    should-run: true
    contexts: null
    labels: null
```

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-liquibase</artifactId>
</dependency>

<!-- For H2 database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- For MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- For PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## 7.2 Configuration Properties

### Complete Configuration
```yaml
spring:
  liquibase:
    # Required: Path to master changelog
    change-log: classpath:db/changelog/master.yaml

    # Enable/disable Liquibase (default: true)
    enabled: true

    # Drop existing tables before creating (default: false)
    drop-first: false

    # Default database schema
    default-schema: public

    # Whether to execute migrations (default: true)
    should-run: true

    # Context filter
    contexts: dev,test

    # Label filter
    labels: production-safe

    # Database-specific parameters
    parameters:
      table.prefix: app_
      schema.name: public

    # Lock configuration
    lock-poll-rate: 5s
    lock-concurrency: 1
```

## 7.3 Customizing SpringLiquibase

### Programmatic Configuration
```java
@Configuration
public class LiquibaseConfiguration {

    @Bean
    public SpringLiquibase liquibase(
            @Qualifier("dataSource") DataSource dataSource,
            LiquibaseProperties properties) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isShouldRun());
        liquibase.setLabels(properties.getLabels());

        // Add parameters
        Properties liquibaseProperties = new Properties();
        liquibaseProperties.setProperty("table.prefix", "app_");
        liquibase.setTargetChartInfoProperties(liquibaseProperties);

        return liquibase;
    }
}
```

### Customizing with Properties
```java
@Configuration
@ConfigurationProperties(prefix = "liquibase")
public class LiquibaseProperties {
    private String changeLog = "classpath:db/changelog/master.yaml";
    private boolean enabled = true;
    private String defaultSchema;
    private String contexts;
    private String labels;
    private Map<String, String> parameters = new HashMap<>();

    // Getters and setters
}
```

## 7.4 Controlling Execution Timing

### Before Hibernate
```java
@Bean
@DependsOn({"entityManagerFactory", "dataSource"})
public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:db/changelog/master.yaml");
    return liquibase;
}
```

### After Hibernate
```java
@Bean
@DependsOn("entityManagerFactory")
public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:db/changelog/master.yaml");
    return liquibase;
}
```

### Custom Execution Order
```java
@Component
public class LiquibaseInitializer
        implements ApplicationRunner, Ordered {

    private final SpringLiquibase liquibase;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Manual execution with custom logic
        try {
            liquibase.update("");
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 7.5 Conditional Execution

### Disable Liquibase
```yaml
spring:
  liquibase:
    enabled: false
```

### Environment-based
```java
@Configuration
public class LiquibaseConfig {

    @Bean
    @ConditionalOnProperty(
        prefix = "spring.liquibase",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
    public SpringLiquibase liquibase(DataSource dataSource) {
        return new SpringLiquibase();
    }
}
```

### Profile-based
```java
@Configuration
@Profile("!test")
public class LiquibaseConfig {
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/master.yaml");
        return liquibase;
    }
}
```

## 7.6 Multiple Datasources

### Primary Datasource
```java
@Configuration
public class DataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public SpringLiquibase primaryLiquibase(
            @Qualifier("dataSource") DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/master.yaml");
        return liquibase;
    }
}
```

### Secondary Datasource
```java
@Configuration
public class SecondaryDataSourceConfig {

    @Bean("secondaryDataSource")
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("secondaryLiquibase")
    public SpringLiquibase secondaryLiquibase(
            @Qualifier("secondaryDataSource") DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/secondary-master.yaml");
        return liquibase;
    }
}
```

## 7.7 Custom ChangeLog Loading

### Custom Resource Loader
```java
@Bean
public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:db/changelog/master.yaml");

    // Custom resource loader for external files
    liquibase.setResourceAccessor(
        new ResourceAccessor() {
            @Override
            public SortedSet<String> search(String path, boolean recursive) {
                // Custom search logic
            }
        });

    return liquibase;
}
```

### DatabaseChangelogTable
```java
@Bean
public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:db/changelog/master.yaml");

    // Custom table names
    liquibase.setDatabaseChangeLogTableName("APP_DATABASECHANGELOG");
    liquibase.setDatabaseChangeLogLockTableName("APP_DATABASECHANGELOGLOCK");

    return liquibase;
}
```

## 7.8 Integration with Spring Security

### Skip Migration in Tests
```java
@TestConfiguration
static class TestLiquibaseConfig {
    @Bean
    @Profile("test")
    public SpringLiquibase testLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/test-master.yaml");
        liquibase.setContexts("test");
        return liquibase;
    }
}
```

### Conditional on Security
```java
@Bean
@ConditionalOnProperty(
    prefix = "liquibase.security.enabled",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:db/changelog/master.yaml");
    return liquibase;
}
```

## 7.9 Health Checks

### Actuator Integration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  health:
    liquibase:
      enabled: true
```

### Custom Health Indicator
```java
@Component
public class LiquibaseHealthIndicator extends HealthIndicator {

    private final SpringLiquibase liquibase;
    private final JdbcTemplate jdbcTemplate;

    @Override
    protected Health health() {
        try {
            // Check if Liquibase is enabled
            Database database = liquibase.getDataSource().getConnection()
                .getMetaData().getDatabaseProductName();

            // Check for pending migrations
            int pending = getPendingMigrations();

            if (pending > 0) {
                return Health.status(
                    Status.of(Status.DOWN, "Pending migrations: " + pending))
                    .build();
            }

            return Health.up()
                .withDetail("database", database)
                .withDetail("pendingMigrations", pending)
                .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }

    private int getPendingMigrations() {
        // Query DATABASECHANGELOG for unexecuted changes
        return 0;
    }
}
```

## 7.10 Events and Listeners

### Liquibase Events
```java
@Component
public class LiquibaseEventListener
        implements ApplicationListener< liquibase.integration.spring.SpringLiquibase> {

    @Override
    public void onApplicationEvent(SpringLiquibase liquibase) {
        // Before Liquibase starts
    }
}
```

### Custom Event Publisher
```java
@Component
public class LiquibaseEventPublisher
        implements liquibase.integration.spring.SpringLiquibase {

    private final SpringLiquibase delegate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void update() throws LiquibaseException {
        eventPublisher.publishEvent(
            new LiquibaseMigrationStartingEvent(this));
        try {
            delegate.update();
            eventPublisher.publishEvent(
                new LiquibaseMigrationCompletedEvent(this));
        } catch (Exception e) {
            eventPublisher.publishEvent(
                new LiquibaseMigrationFailedEvent(this, e));
            throw e;
        }
    }
}
```

## 7.11 Metrics with Micrometer

### Track Migration Metrics
```java
@Component
public class LiquibaseMetrics {

    private final MeterRegistry meterRegistry;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(fixedRate = 60000)
    public void recordMetrics() {
        int executed = getExecutedMigrations();
        int pending = getPendingMigrations();
        long executionTime = getLastExecutionTime();

        meterRegistry.gauge("liquibase.executed", executed);
        meterRegistry.gauge("liquibase.pending", pending);
        meterRegistry.gauge("liquibase.execution.time.ms", executionTime);
    }

    private int getExecutedMigrations() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM DATABASECHANGELOG WHERE EXECUTIONSUCCESS = 'TRUE'",
            Integer.class);
    }

    private int getPendingMigrations() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM DATABASECHANGELOG WHERE EXECUTIONDATE IS NULL",
            Integer.class);
    }

    private long getLastExecutionTime() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT EXECUTIONSECONDS * 1000 FROM DATABASECHANGELOG " +
                "WHERE EXECUTIONSUCCESS = 'TRUE' " +
                "ORDER BY DATEEXECUTED DESC LIMIT 1",
                Long.class);
        } catch (Exception e) {
            return 0;
        }
    }
}
```

## 7.12 Testing with Liquibase

### Test Configuration
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:
  liquibase:
    enabled: true
    contexts: test
    drop-first: true
    change-log: classpath:db/changelog/test-master.yaml
```

### Test with H2
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = {
    "spring.liquibase.enabled=true",
    "spring.liquibase.contexts=test"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void testUserCreation() {
        User user = new User();
        user.setUsername("test");
        repository.save(user);

        assertThat(repository.findByUsername("test")).isNotNull();
    }
}
```

### Integration Test
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.liquibase.enabled=true",
    "spring.liquibase.contexts=integration-test"
})
class LiquibaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testMigrationExecuted() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM DATABASECHANGELOG " +
            "WHERE ID = 'create-users-table'",
            Integer.class);

        assertThat(count).isGreaterThan(0);
    }

    @Test
    void testTableExists() {
        assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_name = 'USERS'",
            Integer.class)).isGreaterThan(0);
    }
}
```

## Summary

| Feature | Configuration |
|---------|---------------|
| Enable/Disable | `spring.liquibase.enabled` |
| ChangeLog Path | `spring.liquibase.change-log` |
| Contexts | `spring.liquibase.contexts` |
| Labels | `spring.liquibase.labels` |
| Drop First | `spring.liquibase.drop-first` |
| Schema | `spring.liquibase.default-schema` |

## Next Steps
- [Chapter 8: Production Best Practices](08-production-best-practices.md)
- Security, performance, and monitoring in production
- High availability and disaster recovery
