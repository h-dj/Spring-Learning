# Chapter 4: Change Types Reference

## Overview
Complete reference of all Liquibase change types for database operations.

## 4.1 Table Operations

### createTable
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createTable:
          tableName: customers
          remarks: "Customer information table"
          columns:
            - column:
                name: id
                type: BIGINT
                autoIncrement: true
                constraints:
                  primaryKey: true
                  nullable: false
            - column:
                name: name
                type: VARCHAR(100)
                constraints:
                  nullable: false
            - column:
                name: email
                type: VARCHAR(255)
                constraints:
                  unique: true
            - column:
                name: status
                type: VARCHAR(20)
                defaultValue: 'ACTIVE'
            - column:
                name: created_at
                type: TIMESTAMP
                defaultValueDate: now()
```

### dropTable
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropTable:
          tableName: temporary_table
          cascadeConstraints: true
```

### renameTable
```yaml
- changeSet:
    id: 3
    author: reid
    changes:
      - renameTable:
          oldTableName: user_accounts
          newTableName: users
```

### truncate
```yaml
- changeSet:
    id: 4
    author: reid
    changes:
      - truncate:
          tableName: audit_logs
```

### mergeColumns
```yaml
- changeSet:
    id: 5
    author: reid
    changes:
      - mergeColumns:
          tableName: users
          column1: first_name
          column2: last_name
          newColumnName: full_name
          joinString: " "
```

## 4.2 Column Operations

### addColumn
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: phone
                type: VARCHAR(20)
            - column:
                name: address
                type: VARCHAR(500)
                defaultValue: null
            - column:
                name: age
                type: INT
                defaultValueNumeric: 0
```

### dropColumn
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropColumn:
          tableName: users
          columnName: temporary_field
```

### renameColumn
```yaml
- changeSet:
    id: 3
    author: reid
    changes:
      - renameColumn:
          tableName: users
          oldColumnName: user_name
          newColumnName: username
```

### modifyDataType
```yaml
- changeSet:
    id: 4
    author: reid
    changes:
      - modifyDataType:
          tableName: users
          columnName: email
          newDataType: VARCHAR(255)
```

### addDefaultValue
```yaml
- changeSet:
    id: 5
    author: reid
    changes:
      - addDefaultValue:
          tableName: users
          columnName: status
          defaultValue: 'PENDING'
```

### dropDefaultValue
```yaml
- changeSet:
    id: 6
    author: reid
    changes:
      - dropDefaultValue:
          tableName: users
          columnName: status
```

## 4.3 Constraint Operations

### addPrimaryKey
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - addPrimaryKey:
          tableName: users
          columnNames: id
          constraintName: pk_users
          clustered: true
```

### dropPrimaryKey
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropPrimaryKey:
          tableName: users
          constraintName: pk_users
```

### addUniqueConstraint
```yaml
- changeSet:
    id: 3
    author: reid
    changes:
      - addUniqueConstraint:
          tableName: users
          columnNames: email
          constraintName: uk_users_email
          deferrable: true
          initiallyDeferred: true
```

### dropUniqueConstraint
```yaml
- changeSet:
    id: 4
    author: reid
    changes:
      - dropUniqueConstraint:
          tableName: users
          constraintName: uk_users_email
```

### addForeignKeyConstraint
```yaml
- changeSet:
    id: 5
    author: reid
    changes:
      - addForeignKeyConstraint:
          constraintName: fk_orders_user
          baseTableName: orders
          baseColumnNames: user_id
          referencedTableName: users
          referencedColumnNames: id
          onDelete: CASCADE
          onUpdate: RESTRICT
          deferrable: true
```

### dropForeignKeyConstraint
```yaml
- changeSet:
    id: 6
    author: reid
    changes:
      - dropForeignKeyConstraint:
          baseTableName: orders
          constraintName: fk_orders_user
```

### addNotNullConstraint
```yaml
- changeSet:
    id: 7
    author: reid
    changes:
      - addNotNullConstraint:
          tableName: users
          columnName: email
```

### dropNotNullConstraint
```yaml
- changeSet:
    id: 8
    author: reid
    changes:
      - dropNotNullConstraint:
          tableName: users
          columnName: email
```

## 4.4 Index Operations

### createIndex
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createIndex:
          tableName: users
          indexName: idx_users_email
          columns:
            - column:
                name: email
            - column:
                name: status
          unique: false
          tablespace: USERS
          filterCondition: status = 'ACTIVE'
```

### dropIndex
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropIndex:
          indexName: idx_users_email
          tableName: users
```

## 4.5 Data Operations

### insert
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - insert:
          tableName: users
          columns:
            - column:
                name: username
                value: admin
            - column:
                name: email
                value: admin@example.com
            - column:
                name: status
                value: ACTIVE

      - insert:
          tableName: users
          columns:
            - column:
                name: username
                value: user1
            - column:
                name: email
                value: user1@example.com
```

### update
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - update:
          tableName: users
          columns:
            - column:
                name: status
                value: INACTIVE
            - column:
                name: updated_at
                valueDate: now()
          where: status = 'PENDING' AND created_at < :threeDaysAgo
```

### delete
```yaml
- changeSet:
    id: 3
    author: reid
    changes:
      - delete:
          tableName: audit_logs
          where: created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)
```

### loadData
```yaml
- changeSet:
    id: 4
    author: reid
    changes:
      - loadData:
          tableName: users
          file: db/changelog/data/users.csv
          separator: ,
          quotchar: '"'
          headersinfile: true
```

### loadUpdateData
```yaml
- changeSet:
    id: 5
    author: reid
    changes:
      - loadUpdateData:
          tableName: configuration
          file: db/changelog/data/config.csv
          primaryKey: config_key
```

### delete
```yaml
- changeSet:
    id: 6
    author: reid
    changes:
      - delete:
          tableName: temporary_data
          where: created_at < NOW() - INTERVAL '7 days'
```

## 4.6 SQL Operations

### sql
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - sql:
          sql: |
            INSERT INTO users (username, email) VALUES
            ('user1', 'user1@example.com'),
            ('user2', 'user2@example.com'),
            ('user3', 'user3@example.com');
          stripComments: false
          splitStatements: true
          endDelimiter: ;
```

### sqlFile
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - sqlFile:
          path: db/changelog/scripts/create_functions.sql
          stripComments: true
          splitStatements: true
          encoding: UTF-8
```

## 4.7 View Operations

### createView
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createView:
          viewName: user_summary
          replaceIfExists: true
          fullDefinition: false
          sql: |
            SELECT u.id, u.username, COUNT(o.id) as order_count
            FROM users u
            LEFT JOIN orders o ON u.id = o.user_id
            GROUP BY u.id, u.username
```

### dropView
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropView:
          viewName: user_summary
```

### renameView
```yaml
- changeSet:
    id: 3
    author: reid
    changes:
      - renameView:
          oldViewName: user_summary
          newViewName: v_user_summary
```

## 4.8 Stored Procedure Operations

### createProcedure
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createProcedure:
          procedureName: calculate_order_total
          comments: "Calculate total amount for an order"
          procedureBody: |
            CREATE PROCEDURE calculate_order_total(IN orderId BIGINT)
            BEGIN
                SELECT SUM(amount) INTO @total
                FROM order_items
                WHERE order_id = orderId;
                SELECT @total AS total_amount;
            END
```

### dropProcedure
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropProcedure:
          procedureName: calculate_order_total
```

## 4.9 Trigger Operations

### createTrigger
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createTrigger:
          triggerName: users_audit_trigger
          tableName: users
          triggerBody: |
            CREATE TRIGGER users_audit_trigger
            AFTER INSERT ON users
            FOR EACH ROW
            BEGIN
                INSERT INTO audit_log (action, table_name, record_id, created_at)
                VALUES ('INSERT', 'users', NEW.id, NOW());
            END
```

### dropTrigger
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropTrigger:
          triggerName: users_audit_trigger
```

## 4.10 Sequence Operations

### createSequence
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createSequence:
          sequenceName: order_sequence
          startValue: 1000
          incrementBy: 1
          minValue: 1
          maxValue: 9999999
```

### dropSequence
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropSequence:
          sequenceName: order_sequence
```

## 4.11 Synonym Operations

### createSynonym
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - createSynonym:
          synonymName: user_alias
          forObjectName: users
          forObjectType: TABLE
```

### dropSynonym
```yaml
- changeSet:
    id: 2
    author: reid
    changes:
      - dropSynonym:
          synonymName: user_alias
```

## 4.12 Tag Database
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - tagDatabase:
          tag: v1.0.0
```

## 4.13 Comment
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - comment:
          comment: "This ChangeSet adds user profile functionality"
```

## 4.14 Empty ChangeSet
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - empty:
```

## 4.15 Stop ChangeSet
```yaml
- changeSet:
    id: 1
    author: reid
    changes:
      - stop:
          message: "Manual intervention required - contact DBA"
```

## Summary

| Category | Change Types |
|----------|-------------|
| Tables | createTable, dropTable, renameTable, truncate |
| Columns | addColumn, dropColumn, renameColumn, modifyDataType |
| Constraints | add/drop PrimaryKey, ForeignKey, Unique, NotNull |
| Indexes | createIndex, dropIndex |
| Data | insert, update, delete, loadData, loadUpdateData |
| SQL | sql, sqlFile |
| Views | createView, dropView, renameView |
| Procedures | createProcedure, dropProcedure |
| Triggers | createTrigger, dropTrigger |
| Sequences | createSequence, dropSequence |

## Next Steps
- [Chapter 5: Rollback Strategies](05-rollback-strategies.md)
- Learn how to safely rollback database changes
- Understand different rollback approaches
