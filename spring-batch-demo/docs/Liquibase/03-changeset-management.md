# Chapter 3: ChangeSet Management

## Overview
Learn how to organize, structure, and manage ChangeSets effectively in a growing project.

## 3.1 Organizing ChangeLogs

### Directory Structure
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml
├── db.changelog-1.0.yaml
├── db.changelog-1.1.yaml
├── tables/
│   ├── users.yaml
│   ├── orders.yaml
│   └── products.yaml
└── data/
    ├── users.csv
    └── config.csv
```

### Master ChangeLog (db.changelog-master.yaml)
```yaml
databaseChangeLog:
  - includeAll:
      path: db/changelog/tables/
  - include:
      file: db/changelog/db.changelog-1.0.yaml
  - include:
      file: db/changelog/db.changelog-1.1.yaml
```

### Version-based Organization
```yaml
# db.changelog-1.0.yaml
databaseChangeLog:
  - changeSet:
      id: 1.0.0-001
      author: reid
      changes:
        - createTable:
            tableName: users
```

## 3.2 ChangeSet Best Practices

### Naming Conventions
```yaml
# Good: Descriptive and ordered
- changeSet:
    id: 001__create-users-table
    author: reid
    changes:
      - createTable:
          tableName: users

- changeSet:
    id: 002__create-orders-table
    author: reid
    changes:
      - createTable:
          tableName: orders

# Bad: Non-descriptive
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: users
```

### Atomic Changesets
```yaml
# Good: Related changes in one ChangeSet
- changeSet:
    id: 003__create-orders-with-constraints
    author: reid
    changes:
      - createTable:
          tableName: orders
      - addPrimaryKey:
          tableName: orders
          columnNames: id
      - addForeignKeyConstraint:
          constraintName: fk_orders_user
          baseTableName: orders
          baseColumnNames: user_id
          referencedTableName: users
          referencedColumnNames: id

# Bad: Related changes split across ChangeSets
- changeSet:
    id: 003a__create-orders-table
    author: reid
    changes:
      - createTable:
          tableName: orders

- changeSet:
    id: 003b__add-fk-constraint
    author: reid
    changes:
      - addForeignKeyConstraint:
          constraintName: fk_orders_user
          baseTableName: orders
          baseColumnNames: user_id
          referencedTableName: users
          referencedColumnNames: id
```

## 3.3 ChangeSet Properties

### runAlways
```yaml
- changeSet:
    id: 1
    author: reid
    runAlways: true
    changes:
      - sql:
          sql: INSERT INTO audit_log (action) VALUES ('app_started');
```

### runOnChange
```yaml
- changeSet:
    id: 2
    author: reid
    runOnChange: true
    changes:
      - sql:
          sql: CREATE OR REPLACE VIEW user_summary AS SELECT * FROM users;
```

### failOnError
```yaml
- changeSet:
    id: 3
    author: reid
    failOnError: false
    changes:
      - sql:
          sql: DROP TABLE IF EXISTS temp_table;
```

### runInTransaction
```yaml
- changeSet:
    id: 4
    author: reid
    runInTransaction: false
    changes:
      - sql:
          sql: INSERT INTO logs VALUES (1); INSERT INTO logs VALUES (2);
```

## 3.4 Comments and Documentation

### ChangeSet Comments
```yaml
- changeSet:
    id: 1
    author: reid
    comments: "Initial users table creation for user authentication"
    changes:
      - createTable:
          tableName: users
```

### Column Comments
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - createTable:
          tableName: users
          remarks: "User accounts for the application"
          columns:
            - column:
                name: id
                type: BIGINT
                remarks: "Primary key, auto-generated"
```

## 3.5 Preconditions

### Database Type Check
```yaml
- changeSet:
    id: 1
    author: reid
    preconditions:
      - precondition:
          onFail: HALT
          dbms:
            type: mysql
    changes:
      - sql:
          sql: SELECT 1;
```

### Table Existence Check
```yaml
- changeSet:
    id: 2
    author: reid
    preconditions:
      - precondition:
          onFail: WARN
          tableExists:
            tableName: users
    changes:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: new_column
                type: VARCHAR(100)
```

### Column Existence Check
```yaml
- changeSet:
    id: 3
    author: reid
    preconditions:
      - precondition:
          onFail: CONTINUE
          columnExists:
            tableName: users
            columnName: email
    changes:
      - modifyDataType:
          tableName: users
          columnName: email
          newDataType: VARCHAR(255)
```

### Combined Preconditions
```yaml
- changeSet:
    id: 4
    author: reid
    preconditions:
      - and:
          - precondition:
              dbms:
                type: postgresql
          - precondition:
              tableExists:
                tableName: users
    changes:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: updated_at
                type: TIMESTAMP
```

## 3.6 View ChangeLog History

### Using Maven
```bash
mvn liquibase:history
```

### Using SQL
```sql
SELECT
    id,
    author,
    filename,
    dateexecuted,
    exectype,
    md5sum,
    description,
    comments
FROM DATABASECHANGELOG
ORDER BY dateexecuted DESC;
```

### Using Spring Boot Actuator
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
```

```java
@Component
public class LiquibaseHealthIndicator extends HealthIndicator {
    private final SpringLiquibase liquibase;

    @Override
    protected Health health() {
        try {
            int pendingCount = getPendingMigrations();
            if (pendingCount > 0) {
                return Health.up()
                    .withDetail("pendingMigrations", pendingCount)
                    .build();
            }
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }

    private int getPendingMigrations() {
        return 0; // Implement based on your needs
    }
}
```

## 3.7 Validating ChangeLogs

### Validate Syntax
```bash
mvn liquibase:validate
```

### Validate in Code
```java
@Service
public class LiquibaseValidator {
    private final SpringLiquibase liquibase;

    @PostConstruct
    public void validate() {
        try {
            liquibase.validate();
            log.info("Liquibase ChangeLog validation successful");
        } catch (LiquibaseException e) {
            log.error("Liquibase validation failed", e);
            throw new RuntimeException(e);
        }
    }
}
```

## 3.8 Skipping ChangeSets

### Using Contexts
```bash
mvn spring-boot:run -Dspring.liquibase.contexts=prod
```

### Using Labels
```bash
mvn spring-boot:run -Dspring.liquibase.labels=initial
```

### Programmatic Control
```java
@Configuration
public class LiquibaseConfig {
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource, Environment env) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(env.getProperty("spring.liquibase.change-log"));

        // Disable Liquibase based on condition
        String enabled = env.getProperty("liquibase.enabled", "true");
        if (!"true".equals(enabled)) {
            return null;
        }

        return liquibase;
    }
}
```

## 3.9 ChangeSet Dependencies

### Implicit Ordering (by file order)
```yaml
# db.changelog-1.0.yaml
- include:
    file: db/changelog/tables/users.yaml
- include:
    file: db/changelog/tables/orders.yaml
```

### Explicit Ordering (using id prefix)
```yaml
# users.yaml
- changeSet:
    id: 100__create-users-table

# orders.yaml
- changeSet:
    id: 200__create-orders-table
    preconditions:
      - precondition:
          tableExists:
            tableName: users
```

## 3.10 Summary

Key takeaways:
- Organize ChangeLogs by version and functionality
- Use descriptive ChangeSet IDs with prefixes
- Keep ChangeSets atomic and focused
- Use preconditions for safety checks
- Validate ChangeLogs before deployment

## Next Steps
- [Chapter 4: Change Types](04-change-types.md)
- Learn about all available database change types
- Master complex database operations
