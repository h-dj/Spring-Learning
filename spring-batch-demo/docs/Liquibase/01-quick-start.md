# Chapter 1: Quick Start with Spring Boot

## Overview
This chapter introduces the minimal setup required to get Liquibase running with Spring Boot.

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- Spring Boot 4.0.1+ project

## Step 1: Add Liquibase Dependency

### Maven (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-liquibase</artifactId>
</dependency>
```

### Gradle (build.gradle)
```gradle
implementation 'org.springframework.boot:spring-boot-starter-liquibase'
```

## Step 2: Configure Liquibase

### application.properties
```properties
# Liquibase Configuration
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.enabled=true
spring.liquibase.default-schema=public
```

### application.yml (Alternative)
```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
    default-schema: public
```

## Step 3: Create Master ChangeLog File

**Location:** `src/main/resources/db/changelog/db.changelog-master.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: reid
      changes:
        - createTable:
            tableName: demo_table
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
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
```

## Step 4: Run the Application

```bash
mvn spring-boot:run
```

**Expected Output:**
```
Started SpringBatchDemoApplication in 2.5 seconds
```

**Verify Database Changes:**
1. Access H2 Console at: `http://localhost:8080/h2-console`
2. JDBC URL: `jdbc:h2:mem:testdb`
3. Connect and verify `demo_table` exists with columns `id` and `name`

## What Happens Under the Hood?

1. **Auto-configuration**: Spring Boot auto-configures Liquibase when the dependency is present
2. **ChangeLog Discovery**: Spring Boot scans `classpath*:/db/changelog/*.yaml` (or `.xml`)
3. **ChangeSet Execution**: Each changeSet is executed sequentially if not already applied
4. **Database Lock**: Liquibase acquires a database lock during migration execution
5. **Checksum Validation**: On next run, Liquibase compares checksums to detect changes

## Next Steps

After completing this chapter:
- Move to [Chapter 2: Core Liquibase Concepts](02-core-concepts.md)
- Learn about ChangeSets, Contexts, and Labels
- Understand the role of Database ChangeLog

## Common Issues

### Issue: Liquibase disabled by default in some Spring Boot versions
**Solution:** Explicitly set `spring.liquibase.enabled=true`

### Issue: Tables not created
**Solution:** Verify changeLog path is correct and file exists in classpath

### Issue: "Another Liquibase process is currently running"
**Solution:** Liquibase locks the database during migration. Check for running processes or corrupted DATABASECHANGELOGLOCK table.
