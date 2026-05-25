# Chapter 8: Production Best Practices

## Overview
Learn best practices for running Liquibase in production environments securely and efficiently.

## 8.1 Security Best Practices

### Environment Variables for Credentials
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Secrets Management
```yaml
# Kubernetes Secrets
apiVersion: v1
kind: Secret
metadata:
  name: db-secrets
type: Opaque
stringData:
  username: admin
  password: "${DB_PASSWORD}"
```

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_SERVICE_HOST}:${POSTGRES_SERVICE_PORT}/app
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Vault Integration
```java
@Configuration
public class VaultConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource(VaultTemplate vaultTemplate) {
        VaultResponse response = vaultTemplate.read("secret/database");
        String url = response.getData().get("url").toString();
        String username = response.getData().get("username").toString();
        String password = response.getData().get("password").toString();

        return DataSourceBuilder.create()
            .url(url)
            .username(username)
            .password(password)
            .build();
    }
}
```

### SSL/TLS for Database Connections
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/app?ssl=true&sslmode=require&sslcert=/path/to/client.crt&sslkey=/path/to/client.key&sslrootcert=/path/to/root.crt

    # MySQL
    url: jdbc:mysql://localhost:3306/app?useSSL=true&requireSSL=true&verifyServerCertificate=true
```

### Credential Rotation
```java
@Component
public class CredentialRotationService {

    private final DataSource dataSource;

    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    public void rotateCredentials() {
        // Implement credential rotation logic
        // Update database users
        // Refresh connection pool
    }
}
```

## 8.2 Performance Optimization

### Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Liquibase-specific Optimization
```java
@Configuration
public class LiquibasePerformanceConfig {

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/master.yaml");
        liquibase.setContexts("prod");

        // Optimize for production
        Properties props = new Properties();
        props.setProperty("liquibase.database.class",
            "liquibase.database.core.PostgresDatabase");
        props.setProperty("liquibase.hub.mode", "off");
        props.setProperty("liquibase.showSummary", "true");
        liquibase.setTargetChartInfoProperties(props);

        return liquibase;
    }
}
```

### Batch Processing
```java
@Configuration
public class BatchLiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/master.yaml");

        // Enable streaming for large changesets
        Properties props = new Properties();
        props.setProperty("liquibase.stream", "true");

        return liquibase;
    }
}
```

### Reducing Lock Contention
```yaml
spring:
  liquibase:
    lock-poll-rate: 5s
    lock-concurrency: 1
```

## 8.3 High Availability

### Deployment Strategies
```yaml
# Blue-Green Deployment
# Deploy new version with new liquibase tag
# Switch traffic after verification

# Rolling Deployment
# Each pod runs liquibase update on startup
# Use version-specific contexts
```

### Preventing Concurrent Migrations
```yaml
spring:
  liquibase:
    lock-concurrency: 1
```

```java
@Service
public class DistributedLockService {

    private final RedissonClient redissonClient;

    @Transactional
    public void executeWithLock(Runnable migration) {
        RLock lock = redissonClient.getLock("liquibase-migration");
        try {
            if (lock.tryLock(10, 60, TimeUnit.MINUTES)) {
                try {
                    migration.run();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("Could not acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
```

### Database Replication
```yaml
# Run migrations on primary
# Application reads from replicas
spring:
  datasource:
    primary:
      url: jdbc:postgresql://primary:5432/app
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
    replica:
      url: jdbc:postgresql://replica1:5432/app
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
```

## 8.4 Monitoring and Alerting

### Logging Configuration
```xml
<!-- logback-spring.xml -->
<configuration>
    <springProperty scope="context" name="appName"
        source="spring.application.name" defaultValue="app"/>

    <appender name="LIQUIBASE_FILE"
        class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/liquibase.log</file>
        <rollingPolicy
            class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/liquibase.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <timeBasedFileNamingAndTriggeringPolicy
                class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="liquibase" level="INFO"/>
    <logger name="liquibase.changelog" level="DEBUG"/>
    <logger name="liquibase.sql" level="DEBUG"/>

    <root level="INFO">
        <appender-ref ref="LIQUIBASE_FILE"/>
    </root>
</configuration>
```

### Monitoring Dashboard
```java
@Service
public class LiquibaseMonitoringService {

    private final JdbcTemplate jdbcTemplate;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 60000)
    public void collectMetrics() {
        // Executed migrations
        Gauge.builder("liquibase.executed.count", this::getExecutedCount)
            .tag("environment", getEnvironment())
            .register(meterRegistry);

        // Pending migrations
        Gauge.builder("liquibase.pending.count", this::getPendingCount)
            .tag("environment", getEnvironment())
            .register(meterRegistry);

        // Lock status
        Gauge.builder("liquibase.locked", this::isLocked)
            .tag("environment", getEnvironment())
            .register(meterRegistry);
    }

    private int getExecutedCount() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM DATABASECHANGELOG WHERE EXECUTIONSUCCESS = 'TRUE'",
            Integer.class);
    }

    private int getPendingCount() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM DATABASECHANGELOG WHERE EXECUTIONDATE IS NULL",
            Integer.class);
    }

    private boolean isLocked() {
        return jdbcTemplate.queryForObject(
            "SELECT LOCKED FROM DATABASECHANGELOGLOCK WHERE ID = 1",
            Boolean.class);
    }
}
```

### Alert Rules
```yaml
# Prometheus alerts
groups:
  - name: liquibase
    rules:
      - alert: LiquibaseMigrationStuck
        expr: liquibase_locked == 1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Liquibase migration is stuck"

      - alert: LiquibasePendingMigrations
        expr: liquibase_pending_count > 0
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "{{ $value }} pending migrations"

      - alert: LiquibaseMigrationFailed
        expr: increase(liquibase_migrations_failed_total[5m]) > 0
        labels:
          severity: critical
        annotations:
          summary: "Liquibase migration failed"
```

## 8.5 Backup and Recovery

### Backup Before Migration
```bash
#!/bin/bash
# backup-before-migration.sh

BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"

# PostgreSQL backup
pg_dump -h $DB_HOST -U $DB_USERNAME -d $DB_NAME > $BACKUP_FILE

# MySQL backup
mysqldump -h $DB_HOST -u $DB_USERNAME -p $DB_NAME > $BACKUP_FILE

# Compress
gzip $BACKUP_FILE

# Upload to S3
aws s3 cp $BACKUP_FILE.gz s3://backups/
```

### Point-in-Time Recovery
```bash
# Restore to specific point
pg_restore -h $DB_HOST -U $DB_USERNAME -d $DB_NAME $BACKUP_FILE

# Rollback to tag
liquibase rollback --tag=v1.0.0
```

### Rollback Scripts
```bash
#!/bin/bash
# rollback-migration.sh

TAG=$1
if [ -z "$TAG" ]; then
    echo "Usage: rollback-migration.sh <tag>"
    exit 1
fi

# Generate rollback SQL first
liquibase rollbackSQL --tag=$TAG > rollback_$TAG.sql

# Review
echo "Review rollback SQL:"
cat rollback_$TAG.sql

# Execute if approved
read -p "Execute rollback? (yes/no): " confirm
if [ "$confirm" = "yes" ]; then
    liquibase rollback --tag=$TAG
    echo "Rollback completed"
fi
```

## 8.6 Change Management

### Approval Workflow
```yaml
# GitOps workflow
# 1. Create migration file in feature branch
# 2. Code review and approval
# 3. Merge to main branch
# 4. CI/CD runs validation
# 5. Manual approval for production
# 6. Deploy to production
```

### Migration Tags
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: users
      - tagDatabase:
          tag: v1.0.0
```

### Version Naming
```
V{MAJOR}_{MINOR}_{PATCH}__{YYYYMMDD}__{description}
Example: V1_2_0__20240121__add_user_profile
```

## 8.7 Compliance and Auditing

### Audit Log Query
```sql
-- Who ran migrations
SELECT
    ID,
    AUTHOR,
    DATEEXECUTED,
    EXECTYPE,
    DESCRIPTION,
    COMMENTS,
    LIQUIBASE
FROM DATABASECHANGELOG
ORDER BY DATEEXECUTED DESC;

-- When were changes applied
SELECT
    DATEEXECUTED,
    AUTHOR,
    ID,
    DESCRIPTION
FROM DATABASECHANGELOG
WHERE DATEEXECUTED BETWEEN '2024-01-01' AND '2024-01-31';
```

### Compliance Report
```java
@Service
public class ComplianceReportService {

    private final JdbcTemplate jdbcTemplate;

    public String generateReport(LocalDate start, LocalDate end) {
        List<Map<String, Object>> migrations = jdbcTemplate.queryForList(
            "SELECT * FROM DATABASECHANGELOG " +
            "WHERE DATEEXECUTED BETWEEN ? AND ? " +
            "ORDER BY DATEEXECUTED",
            start, end);

        StringBuilder report = new StringBuilder();
        report.append("Migration Compliance Report\n");
        report.append("Period: ").append(start).append(" to ").append(end).append("\n");
        report.append("Total migrations: ").append(migrations.size()).append("\n\n");

        for (Map<String, Object> migration : migrations) {
            report.append("ID: ").append(migration.get("ID")).append("\n");
            report.append("Author: ").append(migration.get("AUTHOR")).append("\n");
            report.append("Date: ").append(migration.get("DATEEXECUTED")).append("\n");
            report.append("Type: ").append(migration.get("EXECTYPE")).append("\n");
            report.append("Description: ").append(migration.get("DESCRIPTION")).append("\n\n");
        }

        return report.toString();
    }
}
```

## 8.8 Disaster Recovery

### Recovery Plan
```markdown
## Disaster Recovery Plan

### Scenario: Database Corruption
1. Stop all application instances
2. Restore from latest backup
3. Execute all migrations from scratch
   ```
   liquibase dropAll
   liquibase update
   ```
4. Verify data integrity
5. Restart applications

### Scenario: Failed Migration
1. Identify failed ChangeSet
2. Execute rollback
   ```
   liquibase rollback --tag=previous_stable_tag
   ```
3. Fix migration script
4. Re-execute
   ```
   liquibase update
   ```

### Scenario: Complete Restore
1. Restore database from backup
2. Reset Liquibase tracking
   ```sql
   DELETE FROM DATABASECHANGELOG;
   DELETE FROM DATABASECHANGELOGLOCK;
   ```
3. Re-run all migrations
```

### Runbook Template
```markdown
# Incident: [Title]

## Severity
- Critical / High / Medium / Low

## Description
[Description of the issue]

## Impact
[Impact on users and systems]

## Trigger
[What caused this incident]

## Steps to Resolve
1. [Step 1]
2. [Step 2]
3. [Step 3]

## Verification
[How to verify the fix]

## Post-incident
- [ ] Update documentation
- [ ] Add monitoring
- [ ] Schedule review
```

## 8.9 Deployment Checklist

```markdown
## Pre-deployment Checklist
- [ ] All migrations tested in staging environment
- [ ] Rollback plan reviewed
- [ ] Database backup completed
- [ ] Monitoring alerts configured
- [ ] Team notified of deployment window
- [ ] Rollback script tested

## Deployment Steps
1. [ ] Take database backup
2. [ ] Disable automated deployments
3. [ ] Run migration validation
   ```bash
   liquibase validate
   ```
4. [ ] Execute migration
   ```bash
   liquibase update
   ```
5. [ ] Verify execution
   ```bash
   liquibase status
   ```
6. [ ] Enable automated deployments
7. [ ] Monitor for issues

## Post-deployment
- [ ] Verify application functionality
- [ ] Check monitoring dashboards
- [ ] Confirm no errors in logs
- [ ] Document changes
- [ ] Update runbooks
```

## Summary

| Area | Best Practice |
|------|--------------|
| Security | Use secrets management, SSL/TLS |
| Performance | Connection pooling, batch processing |
| Availability | Distributed locks, HA setup |
| Monitoring | Logging, metrics, alerts |
| Backup | Automated backups, tested restore |
| Compliance | Audit logs, compliance reports |

## Next Steps
- [Chapter 9: Testing Strategies](./09-testing-strategies.md)
- Comprehensive testing approaches
- Integration and unit tests
