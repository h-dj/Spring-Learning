# CLAUDE.md — spring-batch-demo

Behavioral guidelines for this Spring Batch demo project.

**Tradeoff note:** Prioritizes caution over speed; use judgment for trivial fixes.

---

## 1. Think Before Coding

- State assumptions explicitly; ask if uncertain.
- Present multiple interpretations when requirements are ambiguous.
- Push back on overcomplicated approaches.

## 2. Simplicity First

- No features beyond what was asked. No abstractions for single-use code.
- No unrequested flexibility. No error handling for impossible scenarios.
- "Would a senior engineer say this is overcomplicated?" — If yes, rewrite.

## 3. Surgical Changes

- Touch only what the task requires. Match existing style.
- Remove what your changes made unused. Don't clean up pre-existing dead code.

## 4. Goal-Driven Execution

- Transform tasks into verifiable goals: "Write tests, then make them pass."
- For multi-step tasks, state a plan with numbered steps and verification checks.

---

## Project: spring-batch-demo

### Tech Stack

Java 17, Spring Boot 3.5.14, Spring Batch 5, Spring Data JPA, H2, Liquibase, MapStruct 1.6.3, Lombok, JUnit 5.

### Batch Conventions

- Jobs/Steps: explicit name in builder matches bean method name (`JobBuilder("fileJob", repo)`).
- Step-scoped beans (`@StepScope`) for late-binding `#{jobParameters['key']}`.
- Chunk-oriented steps with fault tolerance: `.faultTolerant().skip(FlatFileParseException.class).skipLimit(Integer.MAX_VALUE)`.
- `@Configuration` classes — no `@EnableBatchProcessing` (auto-configured by Spring Boot).
- `spring.batch.job.enabled=false` — jobs do NOT auto-start on boot.
- `spring.batch.jdbc.initialize-schema=never` — Batch metadata tables managed by Liquibase.

### Coding Conventions

- Field names: inline in `DelimitedLineTokenizer.setNames(...)` — do NOT extract to constants.
- SQL: externalize to `src/main/resources/sql/{fileType}-insert.sql`, load via `ResourceLoader` with `@StepScope`.
- Use Lombok `@Data` for DTOs. MapStruct `@Mapper(componentModel = "spring")` for DTO mapping.
- Use `JdbcBatchItemWriter` with `BeanPropertyItemSqlParameterSourceProvider` for DB writes.
- Use `FieldSetMapper<Student>` (custom implementation) — not `BeanWrapperFieldSetMapper`.

### Testing Conventions

- **Unit tests**: Pure JUnit 5. No Spring context. Directly `new` the target class.
- **Integration tests**: `@SpringBatchTest` + `@SpringBootTest`, H2 in-memory, `JobLauncherTestUtils`.
- **Controller tests**: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@MockitoBean JobLauncher`.
- No unit tests for: Entity classes, DTOs, MapStruct-generated implementations, StepExecutionListener (logging only).
- Test data files: `src/test/resources/data/`. Test `@DisplayName` in Chinese.

### Workflow

- After each implementation, update this CLAUDE.md with any new conventions or rules discovered during the work.

### Common Commands

```bash
./mvnw test                           # All tests
./mvnw test -Dtest=StudentProcessorTest   # Single test class
./mvnw spring-boot:run                # Start app
./mvnw clean package -DskipTests      # Package
```
