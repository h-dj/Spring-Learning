# 实现计划 V4：测试数据文件生成工具

## 1. 概述

为 spring-batch-demo 项目创建一个交互式 CLI 工具，通过 YAML 配置驱动生成 `data/{fileType}.dat` 测试数据文件，兼容不同表类型。

**目标：**
- 交互式 CLI，输入 `fileType` 即可从 classpath 加载配置并生成数据文件
- 通过 YAML 配置描述表结构和字段生成规则，支持不同实体/表的扩展
- 六种生成器策略：`values` / `range` / `pattern` / `template` / `fixed` / `random_string`
- 固定 seed 保证数据可重复，支持 `--seed` 参数覆盖
- 支持字段级 `null_probability`，产生空值数据用于测试校验逻辑
- UTF-8 输出，直接覆盖已有文件

---

## 2. 设计决策总表

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 工具定位 | CLI 交互式工具，`main()` 入口 | 独立于 Spring Boot 运行，不依赖应用上下文 |
| 运行方式 | 输入 fileType → classpath `data-config/{fileType}.yaml` → 输出 `data/{fileType}.dat` | 简单直接，与现有 `data/` 目录一致 |
| 配置格式 | YAML（SnakeYAML 解析） | SnakeYAML 已是 Spring Boot 传递依赖，零新增依赖 |
| 配置加载 | SnakeYAML 加载为 `Map`，手动转 POJO | 比注解绑定更可控，错误信息清晰 |
| 随机策略 | `java.util.Random`，默认 seed=42 | 可重复、确定性 |
| 数据编码 | UTF-8 | 与现有数据文件一致 |
| 文件覆盖 | 直接覆盖，不提示 | 工具行为，简洁 |
| 错误处理 | 找不到配置 → 提示重新输入，`q` 退出 | 不中断，友好 |
| 生成器模式 | 策略模式：`FieldValueGenerator` 接口 + 6 个实现 | 易于扩展新生成器类型 |
| 字段生成顺序 | 按 YAML 定义顺序依次生成 | 开发者负责将被引用字段（template）放前面 |
| `null_probability` | 生成器产出值后按概率替换为空串 | 通用修饰，适用于任何生成器类型 |
| 包路径 | `cn.reid.springbatchdemo.datagen` | 独立于业务代码 |
| YAML 解析依赖 | `org.yaml:snakeyaml`（Spring Boot 传递依赖） | pom.xml 无需修改 |

---

## 3. YAML 配置结构

### 3.1 完整示例（`student.yaml`）

```yaml
table: t_student
delimiter: "|"
header: true
rowCount: 20
fields:
  - name: student_no
    type: STRING
    generator:
      pattern: "TEST{auto_increment:3d}"

  - name: name
    type: STRING
    null_probability: 0.1
    generator:
      values: ["张三", "李四", "王五", "赵六", "测试学生A", "测试学生B", "测试学生C"]

  - name: gender
    type: STRING
    generator:
      values: ["M", "F"]

  - name: birth_date
    type: DATE
    null_probability: 0.05
    generator:
      range: ["2000-01-01", "2005-12-31"]

  - name: phone
    type: STRING
    null_probability: 0.1
    generator:
      pattern: "1{random:10d}"

  - name: email
    type: STRING
    generator:
      template: "{name}@email.com"

  - name: class_name
    type: STRING
    generator:
      values: ["测试一班", "测试二班", "测试三班"]

  - name: enrollment_year
    type: INTEGER
    generator:
      range: [2020, 2025]

  - name: status
    type: STRING
    generator:
      fixed: "ACTIVE"
```

### 3.2 配置字段说明

| 配置项 | 必填 | 说明 |
|--------|------|------|
| `table` | Y | 目标表名，仅供文档参考 |
| `delimiter` | N | 字段分隔符，默认 `\|` |
| `header` | N | 是否输出首行字段名，默认 `true` |
| `rowCount` | N | 生成的数据行数，默认 `10` |
| `fields[].name` | Y | 字段名，同时作为输出文件的首行列名 |
| `fields[].type` | Y | 字段类型：`STRING` / `INTEGER` / `DATE` |
| `fields[].null_probability` | N | `0.0` ~ `1.0`，按此概率将值替换为空串 |
| `fields[].generator` | Y | 生成规则，只能包含下方 6 种之一 |

### 3.3 生成器类型对照

| YAML Key | 含义 | 值类型 | 示例 |
|----------|------|--------|------|
| `values` | 轮询取值 | `List<String>` | `["M", "F"]` |
| `range` | 范围内随机 | `List`（2 元素） | `[2020, 2025]` 或 `["2000-01-01", "2005-12-31"]` |
| `pattern` | 模板字符串 + 占位符替换 | `String` | `"TEST{auto_increment:3d}"` |
| `template` | 引用同行其他字段值 | `String` | `"{name}@email.com"` |
| `fixed` | 固定值 | `String` | `"ACTIVE"` |
| `randomString` | 随机字母数字串 | `Integer`（长度） | `8` |

---

## 4. 界面交互流程

```
=== Spring Batch Data File Generator ===
Enter fileType (or 'q' to quit): student
Loading /data-config/student.yaml...
Generating 20 rows...
Done. data/student.dat created (21 lines including header).

Enter fileType (or 'q' to quit): course
Loading /data-config/course.yaml...
Error: data-config/course.yaml not found on classpath.

Enter fileType (or 'q' to quit): q
Bye.
```

命令行参数支持：
- `--seed <number>` — 指定随机种子（默认 42）

---

## 5. 数据生成流程

```
DataFileGenerator.main()
  │
  ├── 解析 --seed 参数
  │
  └── 交互循环：
        ├── 输出提示 "Enter fileType (or 'q' to quit): "
        ├── 读取用户输入
        ├── "q" → break
        ├── 加载 /data-config/{输入}.yaml（classpath）
        │     ├── 找不到 → 输出错误，继续循环
        │     └── 找到 → 解析为 DataGenConfig
        │
        ├── 打开 data/{输入}.dat（UTF-8 Writer）
        │     if header: 写 field1|field2|...\n
        │
        │     for i = 0; i < rowCount; i++:
        │         RowContext ctx = new RowContext(i, random)
        │         rowValues = []
        │         for field in fields:
        │             value = field.generator.generate(ctx)
        │             if null_probability > 0 and random.nextDouble() < null_probability:
        │                 value = ""
        │             ctx.setFieldValue(field.name, value)
        │             rowValues.add(value)
        │         写 String.join(delimiter, rowValues) + "\n"
        │
        └── 输出 "Done. data/{输入}.dat created."
```

### RowContext

| 方法 | 说明 |
|------|------|
| `int getRowIndex()` | 当前行号（0-based） |
| `Random getRandom()` | 共享 `Random` 实例（固定 seed） |
| `void setFieldValue(String name, String value)` | 存储当前行某字段生成值 |
| `String getFieldValue(String name)` | 获取当前行已生成的其他字段值 |

---

## 6. 生成器策略设计

所有生成器实现统一接口：

```java
public interface FieldValueGenerator {
    String generate(RowContext ctx);
}
```

### 6.1 ValuesGenerator

| 项目 | 说明 |
|------|------|
| 配置 | `values: ["A", "B", "C"]` |
| 算法 | `values[rowIndex % values.size()]` |
| 行为 | 每行按顺序取下一个值，轮转循环 |

### 6.2 RangeGenerator

| 项目 | 说明 |
|------|------|
| 配置 | `range: [2020, 2025]`（INTEGER）或 `range: ["2000-01-01", "2005-12-31"]`（DATE） |
| 算法 | INTEGER: `random.nextInt(start, end + 1)` → `String.valueOf()` |
| | DATE: `start.plusDays(random.nextLong(daysBetween + 1))` → `"yyyy-MM-dd"` |
| 异常 | 非 INTEGER/DATE 类型使用 range 时抛异常 |

### 6.3 PatternGenerator — 占位符语法

| 占位符 | 含义 | 示例 |
|--------|------|------|
| `{auto_increment:Nd}` | 自增计数器，N 位零填充，从 1 开始 | `{auto_increment:3d}` → `001`, `002`, `003`... |
| `{random:Nd}` | N 位随机数字 | `{random:10d}` → `3847291056` |

- 占位符外的文本原样保留
- 正则 `\{([^}]+)\}` 匹配占位符
- 自增计数器是 `PatternGenerator` 实例级别的状态（即每个字段独立计数）

### 6.4 TemplateGenerator

| 项目 | 说明 |
|------|------|
| 配置 | `template: "{name}@email.com"` |
| 算法 | 遍历 `\{fieldName\}` 匹配，调用 `ctx.getFieldValue(fieldName)` 替换 |
| 约束 | 被引用字段必须在 YAML 中配置在 template 字段之前 |
| 空值 | 被引用字段值为空时使用空串替换 |

### 6.5 FixedGenerator

| 项目 | 说明 |
|------|------|
| 配置 | `fixed: "ACTIVE"` |
| 行为 | 所有行输出同一固定值 |

### 6.6 RandomStringGenerator

| 项目 | 说明 |
|------|------|
| 配置 | `randomString: 8`（长度） |
| 字符集 | `A-Z, a-z, 0-9`（62 字符） |
| 算法 | 每行使用 `random.nextInt(CHARS.length())` 选取 N 次拼接 |

---

## 7. YAML 加载流程

```
classpath:data-config/{fileType}.yaml
  ↓ SnakeYaml: new Yaml().load(inputStream)
Map<String, Object>
  ↓ DataGenConfig.fromYaml(Map)
DataGenConfig { table, delimiter, header, rowCount, List<FieldConfig> }
  ↓ FieldConfig.fromMap(Map, fieldType)
FieldConfig { name, type, nullProbability, FieldValueGenerator }

generator 工厂判断逻辑（按 key 存在匹配）:
  containsKey("values")       → new ValuesGenerator(List<String>)
  containsKey("range")        → new RangeGenerator(List<Object>, fieldType)
  containsKey("pattern")      → new PatternGenerator(String)
  containsKey("template")     → new TemplateGenerator(String)
  containsKey("fixed")        → new FixedGenerator(String)
  containsKey("randomString") → new RandomStringGenerator(Integer)
  都不匹配                     → 抛 IllegalArgumentException("未知生成器类型")
```

---

## 8. 文件清单

### 8.1 新增文件

| 文件 | 说明 | 代码行数（估） |
|------|------|----------------|
| `src/main/resources/data-config/student.yaml` | Student 表 YAML 配置 | ~40 |
| `src/main/java/cn/reid/springbatchdemo/datagen/DataFileGenerator.java` | 入口 main() + 交互 CLI | ~70 |
| `src/main/java/cn/reid/springbatchdemo/datagen/DataGenConfig.java` | 配置模型 + YAML 解析 + RowContext | ~100 |
| `src/main/java/cn/reid/springbatchdemo/datagen/FieldValueGenerator.java` | 生成器接口 | ~10 |
| `src/main/java/cn/reid/springbatchdemo/datagen/generator/ValuesGenerator.java` | values 策略 | ~20 |
| `src/main/java/cn/reid/springbatchdemo/datagen/generator/RangeGenerator.java` | range 策略 | ~40 |
| `src/main/java/cn/reid/springbatchdemo/datagen/generator/PatternGenerator.java` | pattern 策略 | ~50 |
| `src/main/java/cn/reid/springbatchdemo/datagen/generator/TemplateGenerator.java` | template 策略 | ~30 |
| `src/main/java/cn/reid/springbatchdemo/datagen/generator/FixedGenerator.java` | fixed 策略 | ~15 |
| `src/main/java/cn/reid/springbatchdemo/datagen/generator/RandomStringGenerator.java` | random_string 策略 | ~25 |

### 8.2 不变文件

所有现有文件不变。pom.xml 无需修改（SnakeYAML 已通过 `spring-boot-starter-web` 传递依赖）。

---

## 9. 实施步骤

### 步骤 1：创建 YAML 配置

- 文件：`src/main/resources/data-config/student.yaml`
- 按 3.1 节内容创建，覆盖 Student 实体全部 9 个字段

### 步骤 2：创建 FieldValueGenerator 接口

- 包：`cn.reid.springbatchdemo.datagen`
- 接口：`FieldValueGenerator`，方法 `String generate(RowContext ctx)`

### 步骤 3：创建 6 个生成器实现

- ValuesGenerator、RangeGenerator、PatternGenerator、TemplateGenerator、FixedGenerator、RandomStringGenerator
- 包：`cn.reid.springbatchdemo.datagen.generator`

### 步骤 4：创建 DataGenConfig 模型 + 解析

- 类：`DataGenConfig`（含 `DataGenConfig`、`FieldConfig`、`RowContext` 三个类）
- YAML → `Map<String, Object>` → 手动转 POJO
- Generator 工厂方法根据 Map key 分发到各生成器

### 步骤 5：创建 DataFileGenerator 主类

- 解析 `--seed` 参数
- 交互循环：读输入 → 加载配置 → 生成数据 → 写文件 → 继续
- 异常处理：找不到配置提示重新输入，`q` 退出

---

## 10. 验证清单

| 验证项 | 验证方法 |
|--------|----------|
| 编译通过 | `mvnw clean compile` 无报错 |
| Student 配置语法正确 | 手动检查 YAML 格式 |
| ValuesGenerator 轮询正确 | 3 个值生成 10 行 → 第 0/3/6/9 行取第一个值 |
| RangeGenerator INTEGER 正确 | 1000 行 `range: [2023, 2023]` → 全部为 `2023` |
| PatternGenerator auto_increment 正确 | `{auto_increment:3d}` → 第 0 行 `001`，第 1 行 `002` |
| TemplateGenerator 引用正确 | `{name}@email.com` → name 为"张三"时输出"张三@email.com" |
| null_probability 有效 | `null_probability: 1.0` → 该字段始终为空串 |
| 表头行正确 | `header: true` → 首行为 `student_no\|name\|gender\|...` |
| 分隔符正确 | `delimiter: ","` → 各字段用逗号分隔 |
| seed 可重复 | 相同 seed 两次运行输出完全一致 |
| seed 可覆盖 | `--seed 123` → 输出与默认 seed 不同 |
| 找不到配置 | 输入不存在的 fileType → 显示错误，继续循环 |
| `q` 退出 | 输入 `q` → 程序正常退出 |
