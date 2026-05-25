# 实现计划 V1：单文件 + 单表基础流程

## 来源

Chapter 14 实战分析 — 多文件并发 CPU 高问题排查与优化
目标：从最简单的单 Job + 单文件 + 单 Step 起步，跑通 Spring Batch 完整流程。

## 设计决策

| 决策项 | 结论 | 理由 |
|--------|------|------|
| 起步范围 | 1 Job + 1 文件 + 1 张表 | 先让基本流程跑通，后续逐步增加并发和文件类型 |
| 目标表 | `t_student` | 字段丰富，适合演示 processor 处理和过滤 |
| 文件格式 | `\|` 分隔的文本文件 | 贴合章节描述的线上场景 |
| 文件位置 | `data/student.dat`（项目根目录） | 模拟外部文件传入，API 通过路径参数指定 |
| 文件行数 | 约 50 行 | 够验证流程，不拖慢测试 |
| 并发模式 | 单线程 Step，异步 `TaskExecutorJobLauncher` | 最简 Job Step，Controller 层用 async launcher 返回不阻塞 |
| API 形态 | `POST /api/file/process` | 贴合章节的 API 约束；`JobLauncher.run()` 异步执行 |
| Liquibase | SQL 格式 changelog | 独立 changelog 创建 `t_student` 和 Batch 元数据表 |
| Processor | 数据验证 + 过滤 | 无效记录过滤掉，展示 processor 的业务含义 |
| 开发方式 | TDD（测试先行） | 每组件先定义测试用例，再实现；Entity/DTO/MapStruct 等纯数据容器和代码生成类跳过单元测试 |

## `t_student` 表结构

| 列名 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT (PK, auto_increment) | 主键 |
| `student_no` | VARCHAR(20), NOT NULL, UNIQUE | 学号 |
| `name` | VARCHAR(50), NOT NULL | 姓名 |
| `gender` | CHAR(1) | 性别 (M/F) |
| `birth_date` | DATE | 出生日期 |
| `phone` | VARCHAR(20) | 手机号 |
| `email` | VARCHAR(100) | 邮箱 |
| `class_name` | VARCHAR(50) | 班级名 |
| `enrollment_year` | INT | 入学年份 |
| `status` | VARCHAR(20) | 状态 (ACTIVE/INACTIVE) |

## 示例文件格式 (`data/student.dat`)

```
student_no|name|gender|birth_date|phone|email|class_name|enrollment_year|status
10001|张三|M|2000-01-15|13800138000|zhangsan@email.com|计算机一班|2023|ACTIVE
10002|李四|F|2001-03-20|13900139000|lisi@email.com|计算机一班|2023|ACTIVE
...
```

## 实现步骤

### 第 1 步：基础设施准备

此步骤为所有 TDD 循环提供前置条件（数据库 schema、项目配置、示例数据）。

#### 1a. 创建 Liquibase changelog

- 文件：`src/main/resources/db/changelog/changelog-001-create-t-student.sql`
- 建表 SQL 包含 `t_student` 全部字段，含 UNIQUE 约束 `uk_student_no`
- 文件：`src/main/resources/db/changelog/changelog-002-create-batch-tables.sql`
- 创建 Spring Batch 元数据表（`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION` 等）
- 更新 `db.changelog-master.yaml`，依次引入两个 changelog

#### 1b. 配置 application.properties（开发环境）

- `spring.datasource.url=jdbc:h2:file:./data/spring-batch-demo;MODE=MYSQL`
- `spring.jpa.hibernate.ddl-auto=validate`（表结构由 Liquibase 管理）
- `spring.jpa.show-sql=false`
- `spring.batch.job.enabled=false`（防止启动时自动运行 Job）
- `spring.batch.jdbc.initialize-schema=never`（Batch 表由 Liquibase 管理）
- 启用 H2 Console：`/h2-console`

#### 1c. 创建示例数据文件

- 文件：`data/student.dat`
- 首行为 9 列列头
- 约 50 条数据，覆盖多个班级和入学年份
- 包含少数脏数据（空学号、非法手机号等）

---

### 第 2 步：Student Entity + StudentDTO + StudentMapper（无测试）

> 实体类、DTO、MapStruct 接口均为纯数据容器或代码生成，跳过单元测试。

#### 实现

- 包：`cn.reid.springbatchdemo.entity`
- 类：`Student` — JPA `@Entity` 映射 `t_student`，`@Data` + `@Id` `@GeneratedValue(IDENTITY)`
- 包：`cn.reid.springbatchdemo.dto`
- 类：`StudentDTO` — `@Data` 纯 POJO，字段同 Student
- 类：`FileProcessResponse` — `@Data`，含静态工厂方法 `success()` / `failed()`
- 包：`cn.reid.springbatchdemo.mapper`
- 接口：`StudentMapper` — `@Mapper(componentModel = "spring")`，`StudentDTO toDto(Student)`（自动生成实现，不写单元测试）

---

### 第 3 步：StudentFieldSetMapper（TDD）

#### 测试先行

**StudentFieldSetMapperTest**

| # | 场景 | 输入 FieldSet | 期望 | 断言 |
|---|------|---------------|------|------|
| 1 | 9 个正确字段解析成功 | `["10001","张三","M","2000-01-15","13800138000","zhang@e.com","计算机一班","2023","ACTIVE"]` | Student 对象，所有字段正确映射 | `assertEquals("10001", s.getStudentNo())`, `assertEquals(LocalDate.of(2000,1,15), s.getBirthDate())` |
| 2 | birthDate 格式非法，应返回 null | `["10001","张三","M","not-a-date","13800138000","zhang@e.com","计算机一班","2023","ACTIVE"]` | `birthDate` 为 null，其余字段正常 | `assertNull(s.getBirthDate())`, `assertEquals("张三", s.getName())` |

#### 实现

- 包：`cn.reid.springbatchdemo.mapper`
- 类：`StudentFieldSetMapper implements FieldSetMapper<Student>`
- 解析逻辑：
  - 用 `DelimitedLineTokenizer` 读取 9 个命名列（`studentNo`, `name`, `gender`, `birthDate`, `phone`, `email`, `className`, `enrollmentYear`, `status`）
  - `birthDate` 按 `yyyy-MM-dd` 解析，失败返回 null
  - `enrollmentYear` 用 `readInt("enrollmentYear", 0)` 处理空值

---

### 第 4 步：StudentProcessor（TDD）

#### 测试先行

**StudentProcessorTest**

| # | 场景 | 输入 | 期望 | 断言 |
|---|------|------|------|------|
| 1 | 有效学生记录通过处理 | studentNo="10001", name="张三", gender="M", phone="13800138000" | 返回非空 Student | `assertNotNull(result)` |
| 2 | 学号为 null | studentNo=null | return null | `assertNull(processor.process(s))` |
| 3 | 学号为空白字符串 | studentNo="   " | return null | `assertNull(processor.process(s))` |
| 4 | 姓名为 null | name=null | return null | `assertNull(processor.process(s))` |
| 5 | 姓名为空白字符串 | name="   " | return null | `assertNull(processor.process(s))` |
| 6 | gender="M" → 通过并转为大写 | gender="M" | result.gender="M" | `assertEquals("M", result.getGender())` |
| 7 | gender="F" → 通过并转为大写 | gender="F" | result.gender="F" | `assertEquals("F", result.getGender())` |
| 8 | gender="m" → 小写转大写 | gender="m" | result.gender="M" | `assertEquals("M", result.getGender())` |
| 9 | gender="f" → 小写转大写 | gender="f" | result.gender="F" | `assertEquals("F", result.getGender())` |
| 10 | gender="X" → 非法，过滤 | gender="X" | return null | `assertNull(processor.process(s))` |
| 11 | gender=null → 允许通过 | gender=null | return non-null | `assertNotNull(processor.process(s))` |
| 12 | 有效 11 位手机号 | phone="13800138000" | 通过 | `assertEquals("13800138000", result.getPhone())` |
| 13 | 手机号为 null | phone=null | 通过 | `assertNotNull(processor.process(s))` |
| 14 | 手机号为空字符串 | phone="" | 通过 | `assertNotNull(processor.process(s))` |
| 15 | 手机号不足 11 位 | phone="1380013800" | return null | `assertNull(processor.process(s))` |
| 16 | 手机号含非数字字符 | phone="13800a13800" | return null | `assertNull(processor.process(s))` |
| 17 | status="active" → 转大写 | status="active" | result.status="ACTIVE" | `assertEquals("ACTIVE", result.getStatus())` |
| 18 | status=null → 允许 | status=null | 通过 | `assertNotNull(processor.process(s))` |

#### 实现

- 包：`cn.reid.springbatchdemo.processor`
- 类：`StudentProcessor implements ItemProcessor<Student, Student>`
- 验证逻辑：
  - `studentNo` 为 null 或 blank → return null（过滤）
  - `name` 为 null 或 blank → return null（过滤）
  - `gender` 非空时只接受 M/F（大小写不敏感），否则 return null；空值通过
  - `phone` 非空时校验 11 位纯数字，否则 return null；空值通过
  - `status` 非空时统一转为大写

---

### 第 5 步：FileProcessingMetricsListener

#### 实现（跳过测试设计，纯日志记录无业务分支）

- 包：`cn.reid.springbatchdemo.listener`
- 类：`FileProcessingMetricsListener implements StepExecutionListener`，标注 `@Component`
- `beforeStep`：记录开始时间 + 文件信息（fileType, filePath）
- `afterStep`：计算耗时，输出读取数、写入数（`writeCount`）、过滤数（`readCount - writeCount - skipCount`）、跳过数（`skipCount`）

---

### 第 6 步：FileJobConfig（Reader + Writer + Step + Job）（TDD）

#### 集成测试先行

**FileJobIntegrationTest** — 使用 `@SpringBatchTest` + `@SpringBootTest`，H2 内存库，测试数据文件 `src/test/resources/data/student-test.dat`

测试数据（6 条记录）：

| # | student_no | name | gender | phone | 预期结果 |
|---|-----------|------|--------|-------|---------|
| 1 | TEST001 | 测试学生A | M | 13800138000 | 写入（全部合法） |
| 2 | TEST002 | 测试学生B | F | 13900139000 | 写入（status="active" → 大写） |
| 3 | TEST003 | 测试学生C | m | 13700137000 | 写入（gender "m" → "M"） |
| 4 | TEST004 | (空) | M | 13600136000 | 过滤（name 为空） |
| 5 | TEST005 | 测试学生E | X | 13500135000 | 过滤（gender 非法 "X"） |
| 6 | TEST006 | 测试学生F | F | invalid | 过滤（phone 非法） |

| # | 验证点 | 期望 | 断言 |
|---|--------|------|------|
| 1 | Job 最终状态 | ExitStatus.COMPLETED | `assertEquals(ExitStatus.COMPLETED, execution.getExitStatus())` |
| 2 | Step 读取数 | 6 | `assertEquals(6, stepExecution.getReadCount())` |
| 3 | Step 写入数 | 3 | `assertEquals(3, stepExecution.getWriteCount())` |
| 4 | Step 过滤数 | 3 | `assertEquals(3, filterCount)` |
| 5 | 数据库记录数 | 3 条 | `assertEquals(3, rows.size())` |
| 6 | 第 1 条数据 status | "ACTIVE" | `assertEquals("ACTIVE", row1.get("status"))` |
| 7 | 第 2 条数据 status 大写 | "ACTIVE" | `assertEquals("ACTIVE", row2.get("status"))` |
| 8 | 第 3 条数据 gender 大写 | "M" | `assertEquals("M", row3.get("gender"))` |

#### 实现

- 包：`cn.reid.springbatchdemo.config`
- 类：`FileJobConfig` — `@Configuration`

创建以下 Bean：

**`studentReader(filePath)`** — `@StepScope`，`FlatFileItemReader<Student>`
- `FileSystemResource(filePath)` — 通过 job parameter 传入
- `setLinesToSkip(1)` — 跳过文件表头
- `DefaultLineMapper<Student>` + `DelimitedLineTokenizer("|")`，9 个命名列，`strict=false`
- `StudentFieldSetMapper` 自定义映射

**`studentWriter()`** — `@StepScope`，`JdbcBatchItemWriter<Student>`
- 通过 `@Value("#{jobParameters['fileType']}")` 获取文件类型
- 通过 `ResourceLoader` 动态加载 `classpath:sql/{fileType}-insert.sql`（如 `sql/student-insert.sql`）
- `BeanPropertyItemSqlParameterSourceProvider`

**`fileStep()`**
- Chunk size: 100
- Reader → Processor → Writer
- 注册 `FileProcessingMetricsListener`
- `.faultTolerant().skip(FlatFileParseException.class).skipLimit(Integer.MAX_VALUE)`

**`fileJob()`** — 单 step，name="fileJob"

**`asyncJobLauncher()`** — `@Primary`，`TaskExecutorJobLauncher` + `ThreadPoolTaskExecutor`（core=2, max=4）

**`batchTaskExecutor()`** — `ThreadPoolTaskExecutor`

---

### 第 7 步：FileProcessController（TDD）

#### 集成测试先行

**FileProcessControllerIntegrationTest** — 使用 `@SpringBootTest` + `@AutoConfigureMockMvc`

| # | 场景 | 请求参数 | 期望 | 断言 |
|---|------|---------|------|------|
| 1 | 正常请求，返回 STARTED | fileType="student", filePath="data/student.dat" | HTTP 200, status="STARTED", jobExecutionId > 0 | `status().isOk()`, `jsonPath("$.status").value("STARTED")` |
| 2 | 缺少必要参数 | 不传 fileType 或 filePath | HTTP 400 | `status().isBadRequest()` |

#### 实现

- 包：`cn.reid.springbatchdemo.controller`
- 类：`FileProcessController` — `@RestController`, `@RequestMapping("/api/file")`
- 接口：`POST /api/file/process`
- 参数：`@RequestParam String fileType`, `@RequestParam String filePath`
- 逻辑：构造 `JobParameters`（含 `fileType`, `filePath`, `runTime`），调用 `jobLauncher.run(fileJob, params)`
- 成功返回：`FileProcessResponse.success()`（status="STARTED"）
- 异常返回：`FileProcessResponse.failed()`（status="FAILED"）

---

## 文件清单

```
src/main/java/cn/reid/springbatchdemo/
├── SpringBatchDemoApplication.java            （已有）
├── config/
│   └── FileJobConfig.java                     （已有）
├── controller/
│   └── FileProcessController.java             （已有）
├── dto/
│   ├── FileProcessResponse.java               （已有）
│   └── StudentDTO.java                        （已有）
├── entity/
│   └── Student.java                           （已有）
├── listener/
│   └── FileProcessingMetricsListener.java      （已有）
├── mapper/
│   ├── StudentFieldSetMapper.java              （已有）
│   └── StudentMapper.java                     （已有，MapStruct 接口）
└── processor/
    └── StudentProcessor.java                  （已有）

src/main/resources/
├── application.properties                     （已有）
├── sql/
│   └── student-insert.sql                     （已有，动态加载）
└── db/changelog/
    ├── db.changelog-master.yaml               （已有）
    ├── changelog-001-create-t-student.sql     （已有）
    └── changelog-002-create-batch-tables.sql  （已有）

src/test/java/cn/reid/springbatchdemo/
├── SpringBatchDemoApplicationTests.java        （已有，上下文加载测试）
├── TestSpringBatchDemoApplication.java         （已有，测试入口）
├── TestcontainersConfiguration.java            （已有，Testcontainers 配置）
├── config/
│   └── FileJobIntegrationTest.java            （已有，集成测试 8 个验证点）
├── controller/
│   └── FileProcessControllerIntegrationTest.java （已有，集成测试 2 个场景）
├── mapper/
│   └── StudentFieldSetMapperTest.java         （已有，单元测试 2 个场景）
└── processor/
    └── StudentProcessorTest.java              （已有，单元测试 18 个用例）

src/test/resources/
├── application.properties                     （已有，H2 内存库模式）
└── data/
    └── student-test.dat                       （已有，6 条测试数据，3 写 3 过滤）

data/
└── student.dat                                 （已有，约 50 条）
```

## 后续迭代（不在本版本内）

- V2: 增加其他 4 张表（course, class, exam_score 等）+ 每个表一个 Step 串行执行
- V3: 引入 `SimpleAsyncTaskExecutor` + 多 Job 并发，复现 CPU 高问题
- V4: 引入共享 `ThreadPoolTaskExecutor` 替换，展示优化效果
- V5: 单线程 Step 最佳实践版本 + 对比监控

## 验证方式

### 自动化验证（TDD）

1. `StudentProcessorTest` 运行通过（18 个测试用例覆盖全部验证规则）
2. `StudentFieldSetMapperTest` 运行通过（覆盖核心字段映射场景）
3. `FileJobIntegrationTest` 运行通过（6 条测试数据 → 写入 3 条、过滤 3 条）
4. `FileProcessControllerIntegrationTest` 运行通过（正常请求返回 200，缺少参数返回 400）
5. `./mvnw test` 全部绿色

### 手动验证

1. 启动应用，确认 Liquibase 成功创建 `t_student` 表
2. 调用 `POST /api/file/process?fileType=student&filePath=data/student.dat`
3. 确认返回 JobExecutionId + status="STARTED"
4. 查看日志：读取数、写入数、过滤数是否正确
5. 查询 H2 Console：`t_student` 表中数据正确写入
