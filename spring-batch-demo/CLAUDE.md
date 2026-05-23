# CLAUDE.md — spring-batch-demo

Behavioral guidelines for this Spring Batch demo project. Merge with project-specific sections below.

**Tradeoff note:** These prioritize caution over speed; use judgment for trivial fixes.

---

## 1. Think Before Coding

- State assumptions explicitly before writing code; ask if uncertain.
- Present multiple interpretations when requirements are ambiguous.
- Suggest simpler approaches and push back when the approach feels overcomplicated.
- Stop if confused, name the confusion, and ask.

## 2. Simplicity First

- No features beyond what was asked. No abstractions for single-use code.
- No unrequested flexibility or configurability.
- No error handling for impossible scenarios.
- "Would a senior engineer say this is overcomplicated?" — If yes, rewrite.

## 3. Surgical Changes

- Touch only what the task requires. Match existing style.
- Don't improve adjacent code, comments, or formatting.
- Remove what your changes made unused (imports, variables).
- Don't remove pre-existing dead code unless asked.

## 4. Goal-Driven Execution

- Transform tasks into verifiable goals: "Write tests, then make them pass."
- For multi-step tasks, state a plan with numbered steps and verification checks.
- Strong criteria enable independent work; weak criteria cause constant re-clarification.

---

## Project: spring-batch-demo

### Tech Stack

- Java 17, Spring Boot 3.5.14, Spring Batch 5
- Spring Data JPA + H2 Database + Liquibase
- MapStruct 1.6.3, Lombok
- JUnit 5, Spring Batch Test, Testcontainers

### Batch Configuration

- `spring.batch.job.enabled=false` — Jobs do NOT auto-start on boot
- `spring.batch.jdbc.initialize-schema=never` — Batch metadata tables managed by Liquibase
- `JobLauncher` uses `TaskExecutorJobLauncher` for async execution
- Step uses fault-tolerant + skip strategy for parse exceptions

### Testing Rules

- **Unit tests**: Pure JUnit 5, no Spring context. Directly instantiate target class.
- **Integration tests**: `@SpringBatchTest` + `@SpringBootTest`, H2 in-memory database.
- **MapStruct Mapper接口**：只定义接口，实现由注解处理器自动生成，**不写单元测试**。
- Test data files go under `src/test/resources/data/`.

### Development Rules

- Use Lombok `@Data` for DTO classes.
- MapStruct `@Mapper(componentModel = "spring")` for DTO mapping.
- Test `@DisplayName` in Chinese.
- Configuration classes are plain `@Configuration` — no `@EnableBatchProcessing`.
- Job parameters injected via `@Value("#{jobParameters['key']}")`.

### Common Commands

```bash
./mvnw test                        # All tests
./mvnw test -Dtest=StudentProcessorTest  # Single test
./mvnw spring-boot:run             # Start app
./mvnw clean package -DskipTests   # Package
```
