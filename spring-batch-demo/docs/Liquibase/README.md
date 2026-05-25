# Liquibase Learning Guide

A comprehensive step-by-step learning path for mastering Liquibase database version control.

## Overview

This guide covers Liquibase from basics to production-ready implementation, with practical examples and best practices.

## Learning Path

| Chapter | Topic | Description | Est. Time |
|---------|-------|-------------|-----------|
| [01-quick-start](01-quick-start.md) | Quick Start | Setup Spring Boot with Liquibase | 30 min |
| [02-core-concepts](02-core-concepts.md) | Core Concepts | ChangeLogs, ChangeSets, contexts, labels | 1 hour |
| [03-changeset-management](03-changeset-management.md) | ChangeSet Management | Organization, preconditions, validation | 1 hour |
| [04-change-types](04-change-types.md) | Change Types Reference | Complete change types reference | 2 hours |
| [05-rollback-strategies](05-rollback-strategies.md) | Rollback Strategies | Safe rollback procedures | 1 hour |
| [06-advanced-features](06-advanced-features.md) | Advanced Features | Custom changes, extensions, complex scenarios | 2 hours |
| [07-spring-boot-integration](07-spring-boot-integration.md) | Spring Boot Integration | Auto-configuration, customization | 1 hour |
| [08-production-best-practices](08-production-best-practices.md) | Production Best Practices | Security, monitoring, HA | 2 hours |
| [09-testing-strategies](09-testing-strategies.md) | Testing Strategies | Unit, integration, performance tests | 2 hours |
| [10-ci-cd-integration](10-ci-cd-integration.md) | CI/CD Integration | GitHub Actions, GitLab, Jenkins, K8s | 2 hours |

## Quick Start

1. Add dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-liquibase</artifactId>
</dependency>
```

2. Configure `application.yml`:
```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
```

3. Create changelog `src/main/resources/db/changelog/db.changelog-master.yaml`:
```yaml
databaseChangeLog:
  - changeSet:
      id: 1
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

## Project Structure

```
.claude/docs/Liquibase/
├── README.md                    # This file
├── 01-quick-start.md           # Getting started
├── 02-core-concepts.md         # Fundamentals
├── 03-changeset-management.md  # Organization
├── 04-change-types.md          # Complete reference
├── 05-rollback-strategies.md   # Rollback procedures
├── 06-advanced-features.md     # Advanced topics
├── 07-spring-boot-integration.md # Spring Boot
├── 08-production-best-practices.md # Production
├── 09-testing-strategies.md    # Testing
└── 10-ci-cd-integration.md     # CI/CD
```

## Common Commands

```bash
# Execute migrations
mvn liquibase:update

# Validate change logs
mvn liquibase:validate

# Rollback to tag
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0.0

# Rollback N changesets
mvn liquibase:rollback -Dliquibase.rollbackCount=3

# Generate rollback SQL
mvn liquibase:rollbackSQL -Dliquibase.rollbackTag=v1.0.0

# Check status
mvn liquibase:status

# Clear checksums
mvn liquibase:clearCheckSums
```

## Resources

- [Official Documentation](https://docs.liquibase.com/)
- [GitHub Repository](https://github.com/liquibase/liquibase)
- [Community Forum](https://forum.liquibase.org/)
- [Change Types Reference](https://docs.liquibase.com/change-types.html)

## Estimated Completion

**Total: ~12-14 hours** for complete learning path

## Prerequisites

- Java 17+
- Maven 3.6+
- Basic SQL knowledge
- Spring Boot familiarity (recommended for later chapters)

## Progress Tracking

- [ ] Chapter 1: Quick Start
- [ ] Chapter 2: Core Concepts
- [ ] Chapter 3: ChangeSet Management
- [ ] Chapter 4: Change Types
- [ ] Chapter 5: Rollback Strategies
- [ ] Chapter 6: Advanced Features
- [ ] Chapter 7: Spring Boot Integration
- [ ] Chapter 8: Production Best Practices
- [ ] Chapter 9: Testing Strategies
- [ ] Chapter 10: CI/CD Integration

---

*Part of Spring Batch Demo learning materials*
