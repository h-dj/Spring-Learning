# 实现计划 V5：多表扩展 — Job Decider 路由

## 1. 概述

从单表（Student）扩展为 6 种表类型，使用同一 Job + FileTypeDecider 路由到各自 Step。

**目标：**
- 新增 5 张表：course / class_group / exam_score / enrollment / teacher
- 每个类型独立的 Entity、FieldSetMapper、Processor、Step、SQL insert 文件
- `FileTypeDecider` 根据 `fileType` 路由到对应 Step
- 未知 fileType → failStep，Job FAILED
- 监听器泛型化复用

---

## 2. 设计决策总表

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 架构模式 | 单 Job + Decider → 多 Step | 保持各 Step 强类型独立，互不影响 |
| 路由方式 | `FileTypeDecider` 按 `fileType` 分发 | 明确的条件路由，可读性强 |
| 未知 fileType | 路由到 failStep，抛出异常 → FAILED | 快速失败，避免误处理 |
| 配置组织 | 原 `FileJobConfig` 管 Job/Decider/公共组件，新增 5 个 `XxxStepConfig` | 每类型独立文件，不改动彼此 |
| Chunk size | 统一 500 | 减少重复配置 |
| Fault tolerance | 统一 `.skip(FlatFileParseException.class).skipLimit(Integer.MAX_VALUE)` | 数据源一致，策略一致 |
| 监听器泛型化 | `ItemTimingListener` / `SkipCollectorListener` → `<Object, Object>` | 复用，不放类型 |
| Decider 位置 | `config/FileTypeDecider.java` | 独立文件，职责单一 |
| failStep | `Tasklet` 抛出异常，日志输出 | 确保 FAILED 状态 |

---

## 3. 表结构设计

### 3.1 course — 课程信息

| 字段 | 类型 | 约束 |
|------|------|------|
| id | Long | PK, 自增 |
| course_code | String(20) | NOT NULL, UNIQUE |
| course_name | String(100) | NOT NULL |
| credits | Integer | NOT NULL |
| course_type | String(20) | NOT NULL (REQUIRED/ELECTIVE/PUBLIC) |
| department | String(50) | |
| teacher | String(50) | |
| max_students | Integer | |
| hours | Integer | |
| status | String(10) | ACTIVE/INACTIVE |
| description | String(500) | |

### 3.2 class_group — 班级

| 字段 | 类型 | 约束 |
|------|------|------|
| id | Long | PK, 自增 |
| class_no | String(20) | NOT NULL, UNIQUE |
| class_name | String(100) | NOT NULL |
| grade | Integer | NOT NULL |
| major | String(50) | NOT NULL |
| department | String(50) | |
| head_teacher | String(50) | |
| student_count | Integer | |
| classroom | String(50) | |
| building | String(50) | |
| enrollment_year | Integer | |
| status | String(10) | ACTIVE/GRADUATED/DISSOLVED |

### 3.3 exam_score — 考试成绩

| 字段 | 类型 | 约束 |
|------|------|------|
| id | Long | PK, 自增 |
| student_no | String(20) | NOT NULL |
| course_code | String(20) | NOT NULL |
| score | BigDecimal(5,1) | |
| exam_date | LocalDate | |
| exam_type | String(20) | EXAM/MIDTERM/FINAL/RETAKE |
| credit_points | BigDecimal(3,1) | |
| rank | Integer | |
| passed | String(1) | Y/N |
| comments | String(200) | |
| graded_by | String(50) | |

### 3.4 enrollment — 学生选课

| 字段 | 类型 | 约束 |
|------|------|------|
| id | Long | PK, 自增 |
| student_no | String(20) | NOT NULL |
| course_code | String(20) | NOT NULL |
| semester | String(20) | NOT NULL |
| enrollment_date | LocalDate | |
| status | String(20) | ENROLLED/DROPPED/COMPLETED |
| final_grade | String(2) | A/B/C/D/F |
| attendance_rate | BigDecimal(5,2) | |
| total_attendance | Integer | |
| actual_attendance | Integer | |
| dropped_reason | String(200) | |
| created_at | LocalDateTime | |

### 3.5 teacher — 教师

| 字段 | 类型 | 约束 |
|------|------|------|
| id | Long | PK, 自增 |
| teacher_no | String(20) | NOT NULL, UNIQUE |
| name | String(50) | NOT NULL |
| gender | String(1) | M/F |
| title | String(50) | PROFESSOR/ASSOCIATE/LECTURER/ASSISTANT |
| degree | String(50) | DOCTOR/MASTER/BACHELOR |
| department | String(50) | |
| phone | String(20) | |
| email | String(100) | |
| hire_date | LocalDate | |
| salary_level | Integer | |
| is_advisor | String(1) | Y/N |
| max_courses | Integer | |
| status | String(10) | ACTIVE/LEAVE/RETIRED |

---

## 4. Processor 校验规则

### 4.1 CourseProcessor
- `course_code`: 空 → 过滤
- `course_name`: 空 → 过滤
- `course_type`: 枚举 REQUIRED/ELECTIVE/PUBLIC，非枚举值 → 过滤，转大写
- `status`: 转大写

### 4.2 ClassGroupProcessor
- `class_no`: 空 → 过滤
- `class_name`: 空 → 过滤
- `major`: 空 → 过滤
- `status`: 枚举 ACTIVE/GRADUATED/DISSOLVED，非枚举值 → 过滤，转大写

### 4.3 ExamScoreProcessor
- `student_no`: 空 → 过滤
- `course_code`: 空 → 过滤
- `score`: 非空时 0–100 范围，否则过滤
- `exam_type`: 枚举 EXAM/MIDTERM/FINAL/RETAKE，转大写
- `passed`: Y/N，转大写

### 4.4 EnrollmentProcessor
- `student_no`: 空 → 过滤
- `course_code`: 空 → 过滤
- `semester`: 空 → 过滤
- `status`: 枚举 ENROLLED/DROPPED/COMPLETED，转大写
- `final_grade`: 非空时枚举 A/B/C/D/F，转大写

### 4.5 TeacherProcessor
- `teacher_no`: 空 → 过滤
- `name`: 空 → 过滤
- `title`: 枚举 PROFESSOR/ASSOCIATE/LECTURER/ASSISTANT，转大写
- `phone`: 非空时 11 位数字
- `email`: 非空时含 `@`
- `status`: 枚举 ACTIVE/LEAVE/RETIRED，转大写

---

## 5. 路由流程

```
fileJob
  │
  └── start → FileTypeDecider.decide(jobParams.fileType)
        │
        ├── "student"     ──→ studentStep
        ├── "course"      ──→ courseStep
        ├── "class_group" ──→ classGroupStep
        ├── "exam_score"  ──→ examScoreStep
        ├── "enrollment"  ──→ enrollmentStep
        ├── "teacher"     ──→ teacherStep
        └── default       ──→ failStep (抛出异常 → FAILED)
```

### JobBuilder 路由链

```java
new JobBuilder("fileJob", jobRepository)
    .start(fileTypeDecider)
        .on("STUDENT").to(studentStep)
        .from(fileTypeDecider).on("COURSE").to(courseStep)
        .from(fileTypeDecider).on("CLASS_GROUP").to(classGroupStep)
        .from(fileTypeDecider).on("EXAM_SCORE").to(examScoreStep)
        .from(fileTypeDecider).on("ENROLLMENT").to(enrollmentStep)
        .from(fileTypeDecider).on("TEACHER").to(teacherStep)
        .from(fileTypeDecider).on("UNKNOWN").to(failStep)
    .end()
    .build();
```

---

## 6. 实施步骤（TDD）

### 阶段 1 — 重构基础框架

| # | 先写测试 | 再写代码 | 影响 |
|---|---------|---------|------|
| 1.1 | `FileTypeDeciderTest` | `FileTypeDecider` | 新增 |
| 1.2 | 现有测试验证 | `ItemTimingListener` 泛型化 `<Object, Object>` | 修改 |
| 1.3 | 现有测试验证 | `SkipCollectorListener` 泛型化 `<Object, Object>` | 修改 |
| 1.4 | 整合测试：未知 fileType FAILED | failStep Tasklet、更新 FileJobConfig 路由 | 修改 |

### 阶段 2 — Course（打样）

| # | 先写测试 | 再写代码 | 影响 |
|---|---------|---------|------|
| 2.1 | `CourseProcessorTest` | `CourseProcessor` | 新增 |
| 2.2 | `CourseFieldSetMapperTest` | `CourseFieldSetMapper` | 新增 |
| 2.3 | — | `Course` Entity、`course-insert.sql`、`course.yaml` | 新增 |
| 2.4 | — | `CourseStepConfig` | 新增 |
| 2.5 | `CourseJobIntegrationTest` | —（已有 step 配置后验证全流程） | 新增 |

### 阶段 3 — Teacher

| # | 先写测试 | 再写代码 |
|---|---------|---------|
| 3.1 | `TeacherProcessorTest` | `TeacherProcessor` |
| 3.2 | `TeacherFieldSetMapperTest` | `TeacherFieldSetMapper` |
| 3.3 | — | `Teacher` Entity、`teacher-insert.sql` |
| 3.4 | — | `TeacherStepConfig` |
| 3.5 | `TeacherJobIntegrationTest` | — |

### 阶段 4 — ClassGroup

与 Course/Teacher 完全相同的模式（ProcessorTest → FieldSetMapperTest → Entity/SQL → StepConfig → IntegrationTest）。

### 阶段 5 — ExamScore

同上模式。

### 阶段 6 — Enrollment

同上模式。

---

## 7. 文件清单

### 阶段 1 新增/修改

| 文件 | 操作 |
|------|------|
| `src/main/java/cn/reid/springbatchdemo/config/FileTypeDecider.java` | 新增 |
| `src/main/java/cn/reid/springbatchdemo/config/FileJobConfig.java` | 修改：路由链 + failStep |
| `src/main/java/cn/reid/springbatchdemo/monitor/ItemTimingListener.java` | 修改：泛型化 |
| `src/main/java/cn/reid/springbatchdemo/monitor/SkipCollectorListener.java` | 修改：泛型化 |
| `src/test/java/cn/reid/springbatchdemo/config/FileTypeDeciderTest.java` | 新增 |

### 阶段 2–6 每张表新增

| 目录 | 文件（×5 张表） |
|------|----------------|
| `entity/` | `Course.java`, `ClassGroup.java`, `ExamScore.java`, `Enrollment.java`, `Teacher.java` |
| `dto/` | 对应 5 个 DTO |
| `mapper/` | 对应 5 个 FieldSetMapper |
| `mapper/` | 对应 5 个 MapStruct Mapper |
| `processor/` | 对应 5 个 Processor |
| `config/` | 对应 5 个 StepConfig |
| `sql/` | 对应 5 个 insert SQL 文件 |
| `data-config/` | 对应 5 个 YAML 数据生成配置 |
| `db/changelog/` | 对应 5 个 DDL changelog + 更新 master |
| test `data/` | 对应 5 个 test data 文件 |
| test processors/ | 对应 5 个 ProcessorTest |
| test mappers/ | 对应 5 个 FieldSetMapperTest |
| test config/ | 对应 5 个 JobIntegrationTest |

---

## 8. 验证清单

| 验证项 | 方法 |
|--------|------|
| 6 种 fileType 路由正确 | FileTypeDeciderTest |
| 未知 fileType → FAILED | 整合测试 |
| 监听器泛型化后现有 student 流程不变 | 现有全部测试通过 |
| 每张表 processor 校验规则正确 | XxxProcessorTest |
| 每张表 FieldSetMapper 字段映射正确 | XxxFieldSetMapperTest |
| 每张表全流程集成正常 | XxxJobIntegrationTest（read/write/filter 断言） |
