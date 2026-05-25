# Chapter 2: Core Liquibase Concepts

## Overview
Understanding the fundamental building blocks of Liquibase: ChangeLog, ChangeSet, Contexts, Labels, and Checksums.

## 2.1 ChangeLog (数据库变更日志)

A ChangeLog is the master file that contains all database migrations. It's an ordered list of ChangeSets.

### YAML Format
```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: reid
      changes:
        # migration commands here
  - changeSet:
      id: 2
      author: reid
      changes:
        # another migration
```

### File Naming Convention
- Master ChangeLog: `db.changelog-master.yaml` or `db.changelog-master.xml`
- Versioned ChangeLogs: `changelog/v1.0.0.yaml`, `changelog/v1.1.0.yaml`

## 2.2 ChangeSet (变更集)

A ChangeSet is a single database modification operation. Each ChangeSet has:
- **id**: Unique identifier for this change (not the version)
- **author**: Author name who created the change
- **changes**: The actual database changes
- **context**: Optional, for conditional execution
- **labels**: Optional, for filtering
- **tags**: Optional, for filtering by version

### Example
```yaml
changeSet:
  id: create-users-table-001
  author: reid
  changes:
    - createTable:
        tableName: users
        columns:
          - column:
              name: id
              type: BIGINT
              autoIncrement: true
              constraints:
                primaryKey: true
```

### ChangeSet Properties

| Property | Description | Required |
|-----------|-------------|----------|
| id | Unique ID for this changeSet | Yes |
| author | Author name | Yes |
| changes | The changes to execute | Yes |
| context | Execution context (e.g., "test", "prod") | No |
| labels | Labels for filtering | No |
| runAlways | Force execution every time | No |
| runOnChange | Execute only if file content changes | No |
| runInTransaction | Wrap in single transaction | No |

## 2.3 Contexts (上下文)

Contexts allow you to run migrations selectively based on environment.

### Configuration
```yaml
spring:
  liquibase:
    contexts: test
```

### Usage
```yaml
changeSet:
  id: 1
  author: reid
  changes:
    - createTable:
        tableName: test_table
  context: test

changeSet:
  id: 2
  author: reid
  changes:
    - createTable:
        tableName: prod_table
  context: prod
```

Run only test context:
```bash
mvn spring-boot:run -Dspring.liquibase.contexts=test
```

## 2.4 Labels (标签)

Labels provide another way to filter ChangeSets. Can be used together with Contexts.

### Example
```yaml
changeSet:
  id: 1
  author: reid
  labels:
    - initial
    - data-seed
  changes:
    - createTable:
        tableName: labels_test

changeSet:
  id: 2
  author: reid
  labels:
    - initial
    - indexes
  changes:
    - createIndex:
        tableName: labels_test
        indexName: idx_test
```

## 2.5 Checksums (校验和)

Liquibase stores a checksum for each ChangeSet to detect when files have been modified.

### What is a Checksum?
A hash of the ChangeSet content that helps Liquibase detect if changes have been made.

### When is a Checksum Used?
1. **On first run**: Computes checksum and stores it
2. **On subsequent runs**: Compares stored checksum with current checksum
3. **If mismatch**: Forces re-execution (if configured) or reports error

### Overriding Checksums
If you need to force re-execution without changing content:
```yaml
changeSet:
  id: 1
  author: reid
  changes:
    - createTable:
        tableName: example
  runAlways: true
```

## 2.6 Liquibase Metadata Tables

Liquibase maintains internal tables to track migrations:

| Table | Purpose |
|-------|---------|
| DATABASECHANGELOG | Tracks all ChangeSets and their execution status |
| DATABASECHANGELOGLOCK | Controls concurrent migrations (single writer) |

### Viewing Metadata (H2 Console)
```sql
SELECT * FROM DATABASECHANGELOG;
SELECT * FROM DATABASECHANGELOGLOCK;
```

### Table Structure

**DATABASECHANGELOG:**
```
ID | AUTHOR | FILENAME | DATEEXECUTED | ORDEREXECUTED | EXECTYPE | MD5SUM | COMMENTS | TAG | LIQUIBASE | CONTEXTS | LABELS | DEPLOYMENT_ID
```

**DATABASECHANGELOGLOCK:**
```
ID | LOCKGRANTED | LOCKED | CHECKSUM
```

## 2.7 Liquibase State (状态)

Liquibase maintains a state of your database:
- Which ChangeSets have been executed
- When they were executed
- Current version and tag

### Commands (Command Line)
```bash
# Update state from database
liquibase --driver=org.h2.Driver --url=jdbc:h2:mem:testdb \
         --username=sa --password= \
         --classpath=target/classes updateSQL

# Validate database against ChangeLog
liquibase validate

# Tag current state
liquibase tag --tag=1.0.0

# Rollback to tag
liquibase rollback --tag=1.0.0
```

## Next Steps

- [Chapter 3: ChangeSet Management](03-changeset-management.md)
- Learn about organizing multiple ChangeLog files
- Understand rolling back changes
- Learn about conditions and preconditions
