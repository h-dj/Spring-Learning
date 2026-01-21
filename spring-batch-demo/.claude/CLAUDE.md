# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Spring Batch Demo

**Last Updated:** 2026-01-21
**Technology:** Spring Boot 4.0.1, Java 17, Maven
**Entry Points:** `SpringBatchDemoApplication`

## Project Overview

A minimal Spring Boot starter project demonstrating Spring Batch concepts with JPA, Liquibase, and Testcontainers.

## Technology Stack

### Core Technologies
- **Java**: 17
- **Spring Boot**: 4.0.1

### Dependencies
| Component | Purpose | Source |
|-----------|---------|--------|
| Spring Boot Starter Web MVC | Web application support | Spring Boot Starter |
| Spring Data JPA | ORM with Hibernate | Spring Boot Starter |
| Spring Boot Starter Liquibase | Database schema migration | Spring Boot Starter |
| H2 Console | Database management interface | Spring Boot Starter |
| H2 Database | In-memory database for development/testing | Spring Boot Starter |
| Lombok | Code generation (getters, setters, etc.) | Spring Boot Starter |
| Testcontainers | Integration testing with Docker containers | Spring Boot Starter |

### Testing
- **JUnit 5**: Testing framework
- **Testcontainers**: Docker-based integration testing

## Module Structure

```
src/
├── main/
│   ├── java/cn/reid/springbatchdemo/
│   │   └── SpringBatchDemoApplication.java  # Main application entry point
│   └── resources/
│       └── db/changelog/
│           └── db.changelog-master.yaml    # Liquibase changelogs
└── test/
    └── java/cn/reid/springbatchdemo/
        ├── SpringBatchDemoApplicationTests.java
        ├── TestSpringBatchDemoApplication.java
        └── TestcontainersConfiguration.java  # Testcontainers configuration (proxyBeanMethods = false)
```

## Package Structure

`cn.reid.springbatchdemo` - Single package for all components

## Database

- **Changelogs**: Liquibase YAML format in `src/main/resources/db/changelog/`
- **Sample changelog**: Creates `demo_table` with `id` (BIGINT PK) and `name` (VARCHAR)

## Testing

Spring Boot auto-configuration handles most setup. H2 Console is available at `/h2-console` when application runs.

## Architecture Patterns

### Project Type
- **Starter/Template Project**: Minimal Spring Boot configuration
- **Learning Project**: Educational showcase of Spring Boot features

### Code Organization
- Standard Spring Boot package structure
- Minimal custom configuration

## Key Implementation Details

### Build Configuration
- Lombok annotation processor configured in maven-compiler-plugin
- Spring Boot Maven plugin for executable JAR creation

### Testing
- Integration tests use Testcontainers for Docker-based isolation
- Test configuration uses `@TestConfiguration(proxyBeanMethods = false)`

## Development

**Note:** This is a minimal starter template. Build tools (Maven) are not available in this environment.

When working on a system with Maven available:

### Build Commands
```bash
# Clean and package (executable JAR)
mvn clean package

# Run tests
mvn test

# Run the application
mvn spring-boot:run
```

### Database
- H2 in-memory database (dev/test)
- Liquibase changelogs in `src/main/resources/db/changelog/`
- Changelog master file: `db.changelog-master.yaml`

### Testing
- Test classes require `@Import(TestcontainersConfiguration.class)` for container support
- Use `TestcontainersConfiguration` when configuring test contexts

## Related Documentation

See also:
- `.claude/agents/doc-updater.md` - Documentation and codemap specialist for Java Spring Boot backend projects
- `.claude/docs/` - Additional project documentation (if created)

## Reference Documentation

The project is based on Spring Boot getting started guides:
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)

## Documentation Validation Checklist

- [ ] All documented features have tests
- [ ] Configuration keys are referenced by code
- [ ] Examples pass `mvn test`
- [ ] No undocumented public APIs
- [ ] Package structure matches documentation