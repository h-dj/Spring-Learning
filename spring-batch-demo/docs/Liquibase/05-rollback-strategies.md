# Chapter 5: Rollback Strategies

## Overview
Learn how to rollback database changes safely using Liquibase's rollback capabilities.

## 5.1 Rollback Fundamentals

### Why Rollback?
- Fix failed migrations
- Revert to previous database state
- Point-in-time recovery
- Emergency rollback in production

### Rollback Types
1. **Tag-based Rollback**: Rollback to a tagged state
2. **Count-based Rollback**: Rollback N ChangeSets
3. **Date-based Rollback**: Rollback to a specific date
4. **SQL-based Rollback**: Generate rollback SQL without executing

## 5.2 Tag-based Rollback

### Creating a Tag
```bash
# Maven
mvn liquibase:tag -Dliquibase.tag=v1.0.0

# Command line
liquibase tag --tag=v1.0.0
```

### Using Tags in YAML
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

### Rolling Back to Tag
```bash
# Maven
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0.0

# Command line
liquibase rollback --tag=v1.0.0
```

### Programmatic Tagging
```java
@Service
public class LiquibaseService {
    private final SpringLiquibase liquibase;

    public void tagDatabase(String tag) throws LiquibaseException {
        liquibase.tag(tag);
    }

    public void rollbackToTag(String tag) throws LiquibaseException {
        liquibase.rollback(tag, "");
    }
}
```

## 5.3 Count-based Rollback

### Rolling Back N ChangeSets
```bash
# Maven - rollback last 3 ChangeSets
mvn liquibase:rollback -Dliquibase.rollbackCount=3

# Command line
liquibase rollbackCount 3
```

### Programmatic
```java
public void rollbackLastChanges(int count) throws LiquibaseException {
    liquibase.rollback(count, "");
}
```

## 5.4 Date-based Rollback

### Rolling Back to Date
```bash
# Maven
mvn liquibase:rollback -Dliquibase.rollbackDate=2024-01-15

# Command line
liquibase rollbackToDate 2024-01-15
```

### Format Examples
```bash
# Specific date
-Dliquibase.rollbackDate=2024-01-15

# Date with time
-Dliquibase.rollbackDate="2024-01-15 10:30:00"

# Relative (in rollback SQL generation)
liquibase futureRollbackSQL --tag=v1.0.0
```

## 5.5 Manual Rollback

### Using rollback Tag
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: temp_table
    rollback: |
      DROP TABLE temp_table;
```

### Using rollback with Specific Changes
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: temp_field
                type: VARCHAR(50)
    rollback: |
      ALTER TABLE users DROP COLUMN temp_field;
```

### Multi-statement Rollback
```yaml
- changeSet:
    id: 3
    author: reid
    changes:
      - createTable:
          tableName: orders
      - createTable:
          tableName: order_items
      - addForeignKeyConstraint:
          constraintName: fk_order_items_order
          baseTableName: order_items
          baseColumnNames: order_id
          referencedTableName: orders
          referencedColumnNames: id
    rollback: |
      ALTER TABLE order_items DROP CONSTRAINT fk_order_items_order;
      DROP TABLE order_items;
      DROP TABLE orders;
```

## 5.6 Generated Rollback

### Auto-generated Rollback for Supported Changes
```yaml
# For createTable, Liquibase auto-generates DROP TABLE
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: audit_log
```

### Rollback SQL Preview
```bash
# Generate rollback SQL without executing
mvn liquibase:rollbackSQL -Dliquibase.rollbackTag=v1.0.0

# Output to file
mvn liquibase:rollbackSQL -Dliquibase.rollbackTag=v1.0.0 > rollback.sql
```

### Verify Before Execution
```bash
# Review generated SQL
cat rollback.sql

# Then execute if correct
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0.0
```

## 5.7 Rollback Limitations

### Operations Without Auto-rollback
```yaml
# These require explicit rollback
- changeSet:
    id: 1
    author: reid
    changes:
      - insert:
          tableName: users
          columns:
            - column:
                name: username
                value: test_user
    rollback: |
      DELETE FROM users WHERE username = 'test_user';

- changeSet:
    id: 2
    author: reid
    changes:
      - sql:
          sql: DELETE FROM users WHERE status = 'inactive';
    rollback: |
      -- Cannot auto-generate, must specify
      INSERT INTO users (id, username, status)
      SELECT id, username, 'inactive' FROM users
      WHERE status = 'active' AND deleted_at IS NOT NULL;
```

### Complex Data Changes
```yaml
- changeSet:
    id: 3
    author: reid
    changes:
      - sql:
          sql: |
            UPDATE users u
            JOIN orders o ON u.id = o.user_id
            SET u.total_orders = (
                SELECT COUNT(*) FROM orders WHERE user_id = u.id
            )
    rollback: |
      -- Store backup before update
      -- Complex rollback requiring data preservation
      UPDATE users u
      SET total_orders = (
          SELECT COUNT(*) FROM orders WHERE user_id = u.id
      );
```

## 5.8 Rollback Safety

### Pre-rollback Checklist
```java
@Service
public class RollbackService {
    private final JdbcTemplate jdbcTemplate;
    private final SpringLiquibase liquibase;

    public void safeRollback(String tag) throws Exception {
        // 1. Check if tag exists
        if (!tagExists(tag)) {
            throw new IllegalArgumentException("Tag not found: " + tag);
        }

        // 2. Check for concurrent operations
        if (isLocked()) {
            throw new IllegalStateException("Liquibase is currently locked");
        }

        // 3. Take backup snapshot
        takeBackup();

        // 4. Generate and review rollback SQL
        String rollbackSql = generateRollbackSql(tag);

        // 5. Execute rollback
        executeRollback(tag);
    }

    private boolean tagExists(String tag) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DATABASECHANGELOG WHERE TAG = ?",
                Integer.class, tag) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isLocked() {
        return jdbcTemplate.queryForObject(
            "SELECT LOCKED FROM DATABASECHANGELOGLOCK WHERE ID = 1",
            Boolean.class);
    }

    private void takeBackup() {
        // Implement backup logic
    }
}
```

### Rollback Confirmation
```java
@Component
public class RollbackConfirmation {
    private final JdbcTemplate jdbcTemplate;

    public boolean confirmRollback(String tag) {
        // Get current position
        int currentCount = getChangeLogCount();
        int targetCount = getChangeLogCountAtTag(tag);

        // Show what will be rolled back
        List<Map<String, Object>> changes = getChangesToRollback(tag);
        changes.forEach(change -> {
            log.info("Will rollback: {} by {} - {}",
                change.get("id"),
                change.get("author"),
                change.get("description"));
        });

        return targetCount < currentCount;
    }
}
```

## 5.9 Rollback Testing

### Test Rollback in Development
```bash
# Create test database
# Apply changes
mvn spring-boot:run

# Verify changes
# Rollback
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0.0

# Verify rollback
```

### Automated Rollback Tests
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.liquibase.enabled=true"
})
class RollbackTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Commit
    void testRollbackCapability() {
        // Apply changes
        applyChanges();

        // Record tag
        String tag = "test-tag-" + System.currentTimeMillis();
        tagDatabase(tag);

        // Make more changes
        applyAdditionalChanges();

        // Rollback
        rollbackToTag(tag);

        // Verify
        assertFalse(tableExists("new_table_after_tag"));
    }
}
```

## 5.10 Rollback Best Practices

### 1. Always Test Rollback
```yaml
# Include rollback tests in CI/CD
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: production_table
    rollback: |
      DROP TABLE production_table;
```

### 2. Document Rollback Strategy
```yaml
- changeSet:
    id: 2
    author: reid
    comments: |
      This ChangeSet adds user preferences table.
      Rollback: Simply drop the table. No data loss as this is a new table.
    changes:
      - createTable:
          tableName: user_preferences
```

### 3. Backup Before Risky Migrations
```bash
#!/bin/bash
# pre-migration-backup.sh

# Create backup
pg_dump -h localhost -U user database > backup_$(date +%Y%m%d_%H%M%S).sql

# Run migration
mvn liquibase:update

# Verify
mvn liquibase:status
```

### 4. Use Tag for Major Releases
```yaml
# After completing all changes for v1.0.0
- changeSet:
    id: 100__finalize-v1.0.0
    author: reid
    changes:
      - tagDatabase:
          tag: v1.0.0
```

## 5.11 Emergency Rollback Procedure

### Step 1: Assess Situation
```sql
-- Check recent changes
SELECT * FROM DATABASECHANGELOG
ORDER BY DATEEXECUTED DESC
LIMIT 10;

-- Check for errors
SELECT * FROM DATABASECHANGELOG
WHERE EXECUTIONSUCCESS = 'FALSE';
```

### Step 2: Choose Rollback Method
```bash
# Option 1: Rollback to tag
liquibase rollback --tag=v1.0.0

# Option 2: Rollback N changes
liquibase rollbackCount 3

# Option 3: Rollback to date
liquibase rollbackToDate "2024-01-15 10:00:00"
```

### Step 3: Execute Rollback
```bash
# Generate SQL first for review
liquibase rollbackSQL --tag=v1.0.0 > emergency-rollback.sql

# Review SQL
# Execute if safe
liquibase rollback --tag=v1.0.0
```

### Step 4: Verify
```sql
-- Check data integrity
SELECT COUNT(*) FROM critical_table;

-- Check for any failed operations
SELECT * FROM DATABASECHANGELOG
WHERE DATEEXECUTED > '2024-01-15';
```

## 5.12 Rollback in CI/CD

### GitHub Actions Rollback
```yaml
name: Emergency Rollback

on:
  workflow_dispatch:
    inputs:
      tag:
        description: 'Tag to rollback to'
        required: true

jobs:
  rollback:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Rollback Database
        run: |
          mvn liquibase:rollback \
            -Dliquibase.url=${{ secrets.DB_URL }} \
            -Dliquibase.username=${{ secrets.DB_USER }} \
            -Dliquibase.password=${{ secrets.DB_PASSWORD }} \
            -Dliquibase.rollbackTag=${{ github.event.inputs.tag }}
```

## Summary

| Rollback Type | Use Case | Command |
|---------------|----------|---------|
| Tag-based | Release rollback | `rollback --tag=v1.0.0` |
| Count-based | Quick undo | `rollbackCount 3` |
| Date-based | Point-in-time | `rollbackToDate 2024-01-15` |
| Manual | Complex changes | `rollback` block in YAML |
| Generated | Auto-rollback | Auto-generated for DDL |

## Next Steps
- [Chapter 6: Advanced Features](06-advanced-features.md)
- Learn about custom changes and extensions
- Master complex migration scenarios
