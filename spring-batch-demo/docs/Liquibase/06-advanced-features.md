# Chapter 6: Advanced Features

## Overview
Explore advanced Liquibase features including custom changes, extensions, and complex scenarios.

## 6.1 Custom Changes

### Simple Custom Task Change
```java
package com.example.liquibase.custom;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.sql.PreparedStatement;

public class ArchiveOldDataChange implements CustomTaskChange {
    private String tableName;
    private int daysToKeep;
    private String archiveTableName;

    @Override
    public void setUp() {
        // Initialize configuration
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // Set resource accessor
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = new ValidationErrors();
        if (tableName == null) {
            errors.addError("tableName is required");
        }
        if (daysToKeep <= 0) {
            errors.addError("daysToKeep must be positive");
        }
        return errors;
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection connection = (JdbcConnection) database.getConnection();
        try {
            // Archive data
            String archiveSql = String.format(
                "INSERT INTO %s SELECT * FROM %s WHERE created_at < DATE_SUB(NOW(), INTERVAL %d DAY)",
                archiveTableName, tableName, daysToKeep);
            try (PreparedStatement stmt = connection.prepareStatement(archiveSql)) {
                stmt.execute();
            }

            // Delete archived data
            String deleteSql = String.format(
                "DELETE FROM %s WHERE created_at < DATE_SUB(NOW(), INTERVAL %d DAY)",
                tableName, daysToKeep);
            try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
                stmt.execute();
            }
        } catch (Exception e) {
            throw new CustomChangeException("Failed to archive data", e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return String.format("Archived data older than %d days from %s to %s",
            daysToKeep, tableName, archiveTableName);
    }

    @Override
    public void undo(Database database) throws CustomChangeException {
        // Implement undo logic if possible
    }

    // Getters and Setters
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setDaysToKeep(int daysToKeep) {
        this.daysToKeep = daysToKeep;
    }

    public void setArchiveTableName(String archiveTableName) {
        this.archiveTableName = archiveTableName;
    }
}
```

### Custom SQL Change
```java
package com.example.liquibase.custom;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class CreatePartitionChange implements CustomSqlChange {
    private String tableName;
    private String partitionName;
    private String partitionCondition;

    @Override
    public Sql[] generateStatements(Database database) throws CustomChangeException {
        String sql = String.format(
            "CREATE TABLE %s PARTITION OF %s FOR VALUES %s",
            partitionName, tableName, partitionCondition);
        return new Sql[]{new UnparsedSql(sql)};
    }

    @Override
    public String getConfirmationMessage() {
        return "Created partition " + partitionName + " for table " + tableName;
    }

    // Getters and Setters
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public void setPartitionCondition(String partitionCondition) {
        this.partitionCondition = partitionCondition;
    }
}
```

### Using Custom Change in YAML
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - customChange:
          class: com.example.liquibase.custom.ArchiveOldDataChange
          tableName: audit_logs
          daysToKeep: 90
          archiveTableName: audit_logs_archive

      - customChange:
          class: com.example.liquibase.custom.CreatePartitionChange
          tableName: orders
          partitionName: orders_2024_01
          partitionCondition: "(FROM ('2024-01-01') TO ('2024-02-01'))"
```

## 6.2 Custom Rollback

### CustomTaskRollback
```java
public class DataMigrationChange implements CustomTaskChange {
    private String sourceTable;
    private String targetTable;

    @Override
    public void execute(Database database) throws CustomChangeException {
        // Migration logic
    }

    @Override
    public void undo(Database database) throws CustomChangeException {
        // Reverse migration
        JdbcConnection connection = (JdbcConnection) database.getConnection();
        try {
            // Move data back
            String undoSql = String.format(
                "INSERT INTO %s SELECT * FROM %s", sourceTable, targetTable);
            try (PreparedStatement stmt = connection.prepareStatement(undoSql)) {
                stmt.execute();
            }
        } catch (Exception e) {
            throw new CustomChangeException("Failed to undo migration", e);
        }
    }
}
```

## 6.3 Extensions

### Creating a Liquibase Extension
```java
package com.example.liquibase.extension;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;

@DatabaseChange(name = "addComputedColumn",
                description = "Adds a computed/virtual column",
                priority = 4)
public class AddComputedColumnChange extends AbstractChange {
    private String tableName;
    private String columnName;
    private String expression;

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = new ValidationErrors();
        if (tableName == null) errors.addError("tableName is required");
        if (columnName == null) errors.addError("columnName is required");
        if (expression == null) errors.addError("expression is required");
        return errors;
    }

    @Override
    public String getConfirmationMessage() {
        return "Added computed column " + columnName + " to " + tableName;
    }

    @Override
    protected Change[] generateStatements(Database database) {
        // Implement for different databases
        return new Change[0];
    }

    @Override
    protected Change[] generateRollbackStatements(Database database) {
        return new Change[0];
    }
}
```

## 6.4 Executable SQL

### Executable Statement
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - executableChange:
          class: com.example.liquibase.executable.UpdateStatisticsChange
```

### Executable Change Implementation
```java
public class UpdateStatisticsChange extends AbstractExecutableChange {
    private String schemaName;
    private String tableName;

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            String sql = "ANALYZE TABLE " + tableName;
            if (schemaName != null) {
                sql = "ANALYZE TABLE " + schemaName + "." + tableName;
            }
            ((JdbcConnection) database.getConnection())
                .createStatement()
                .execute(sql);
        } catch (Exception e) {
            throw new CustomChangeException("Failed to update statistics", e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Updated statistics for table: " + tableName;
    }

    @Override
    public void setUp() throws SetupException {
        super.setUp();
    }

    @Override
    protected String getConfirmationMessage0() {
        return getConfirmationMessage();
    }
}
```

## 6.5 Complex Preconditions

### Nested Preconditions
```yaml
- changeSet:
    id: 1
    author: reid
    preconditions:
      - and:
          - precondition:
              tableExists:
                tableName: users
          - or:
              - precondition:
                  columnExists:
                    tableName: users
                    columnName: email
              - precondition:
                  columnExists:
                    tableName: users
                    columnName: email_address
          - not:
              precondition:
                rowCount:
                  tableName: users
                  expectedRows: 0
    changes:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: notification_preference
                type: VARCHAR(50)
```

### Custom Preconditions
```java
public class DataExistsPrecondition implements CustomPrecondition {
    private String tableName;
    private String whereClause;

    @Override
    public void check(Database database) throws CustomPreconditionFailedException {
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName;
            if (whereClause != null) {
                sql += " WHERE " + whereClause;
            }
            JdbcConnection conn = (JdbcConnection) database.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (!rs.next() || rs.getInt(1) == 0) {
                throw new CustomPreconditionFailedException(
                    "No data found matching condition");
            }
        } catch (Exception e) {
            throw new CustomPreconditionFailedException(
                "Precondition check failed", e);
        }
    }
}
```

## 6.6 Database-specific Changes

### Conditional ChangeSets
```yaml
- changeSet:
    id: 1
    author: reid
    preconditions:
      - precondition:
          onFail: CONTINUE
          dbms:
            type: postgresql
    changes:
      - sql:
          sql: CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
          dbms: postgresql

- changeSet:
    id: 2
    author: reid
    preconditions:
      - precondition:
          onFail: CONTINUE
          dbms:
            type: mysql
    changes:
      - sql:
          sql: SET GLOBAL log_bin_trust_function_creators = 1;
          dbms: mysql
```

### Platform-aware DDL
```yaml
- changeSet:
    id: 3
    author: reid
    changes:
      - createTable:
          tableName: jwt_tokens
          columns:
            - column:
                name: token
                type: TEXT
                constraints:
                  nullable: false
            - column:
                name: expires_at
                type: TIMESTAMP
          ifNotExists: true
          dbms: postgresql

      - createTable:
          tableName: jwt_tokens
          columns:
            - column:
                name: token
                type: TEXT
                constraints:
                  nullable: false
            - column:
                name: expires_at
                type: DATETIME
          dbms: mysql
```

## 6.7 Dynamic Values

### Using Properties
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - insert:
          tableName: configuration
          columns:
            - column:
                name: config_key
                value: "${app.name}"
            - column:
                name: config_value
                value: "${app.version}"
```

### Parameter Substitution
```yaml
spring:
  liquibase:
    parameters:
      table.prefix: app_
      schema.name: public

# In changeLog:
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: ${table.prefix}users
          columns:
            - column:
                name: id
                type: BIGINT
```

## 6.8 Snapshot and Diff

### Taking a Snapshot
```bash
mvn liquibase:snapshot -Dliquibase.snapshot=baseline.json
```

### Generating Diff
```bash
mvn liquibase:diff -Dliquibase.diffChangeLog=diff-output.yaml
```

### Compare Against Reference
```bash
mvn liquibase:diff \
  -Dliquibase.referenceUrl=jdbc:postgresql://localhost:5432/reference_db \
  -Dliquibase.referenceUsername=user \
  -Dliquibase.referencePassword=pass
```

### Programmatic Diff
```java
@Service
public class LiquibaseDiffService {
    private final SpringLiquibase liquibase;

    public String generateDiff(String referenceUrl, String referenceUser,
                               String referencePass) throws Exception {
        Database reference = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(
                new JdbcConnection(
                    DriverManager.getConnection(
                        referenceUrl, referenceUser, referencePass)));

        Database current = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(
                new JdbcConnection(
                    liquibase.getDataSource().getConnection()));

        CompareControl compareControl = new CompareControl();
        DiffResult result = Diff.compare(reference, current, compareControl);

        return DiffToChangeChangeLogSerializer
            .serialize(result.getChanges());
    }
}
```

## 6.9 Selective Execution

### Context-based Execution
```yaml
- changeSet:
    id: 1
    author: reid
    context: dev,staging
    changes:
      - createTable:
          tableName: test_data

- changeSet:
    id: 2
    author: reid
    context: production
    changes:
      - createTable:
          tableName: analytics_events
```

### Label-based Execution
```yaml
- changeSet:
    id: 1
    author: reid
    labels: critical,breaking-change
    changes:
      - dropColumn:
          tableName: users
          columnName: old_field
```

### Run Conditions
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: feature_flags
    runOnChange: true

- changeSet:
    id: 2
    author: reid
    changes:
      - sql:
          sql: SELECT 1
    runAlways: true
```

## 6.10 Lock Management

### Understanding the Lock
```sql
-- Check lock status
SELECT * FROM DATABASECHANGELOGLOCK;

-- Lock holder
SELECT id, LOCKED, LOCKGRANTED, LOCKEDBY
FROM DATABASECHANGELOGLOCK;

-- Waiting processes
SELECT pg_stat_activity.state,
       pg_stat_activity.query,
       pg_stat_activity.wait_event
FROM pg_stat_activity
WHERE state != 'idle';
```

### Manual Unlock (Emergency)
```sql
-- ONLY use in emergency situations
UPDATE DATABASECHANGELOGLOCK
SET LOCKED = FALSE,
    LOCKGRANTED = NULL,
    LOCKEDBY = NULL
WHERE ID = 1;
```

### Lock Timeout Configuration
```yaml
spring:
  liquibase:
    parameters:
      lockWaitTime: 10m
```

## 6.11 ChangeLog Parameters

### Global Parameters
```yaml
spring:
  liquibase:
    parameters:
      schema.name: public
      table.prefix: app_
     LOB.enabled: true

# Usage in YAML:
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: ${table.prefix}users
          schemaName: ${schema.name}
```

### Per-changeSet Parameters
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: users
    param:
      name: table.type
      value: innodb
```

## 6.12 Ignore Patterns

### Ignore Errors
```yaml
- changeSet:
    id: 1
    author: reid
    failOnError: false
    changes:
      - sql:
          sql: DROP TABLE IF EXISTS legacy_table
```

### Ignore Warnings
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - sql:
          sql: INSERT INTO logs VALUES (1)
      - modifyDataType:
          tableName: users
          columnName: name
          newDataType: VARCHAR(200)
          ignoreWarnings: true
```

## Summary

| Feature | Use Case |
|---------|----------|
| Custom Changes | Complex business logic migrations |
| Extensions | Add new change types |
| Preconditions | Safety checks before migration |
| Snapshots | Baseline database state |
| Diff | Generate changes from comparisons |
| Lock Management | Concurrent execution control |

## Next Steps
- [Chapter 7: Spring Boot Integration](07-spring-boot-integration.md)
- Deep dive into Spring Boot Liquibase auto-configuration
- Custom configurations and best practices
