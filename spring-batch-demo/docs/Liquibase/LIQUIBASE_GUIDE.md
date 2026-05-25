# Liquibase 学习指南：从入门到生产环境实战

## 目录
1. [什么是 Liquibase](#1-什么是-liquibase)
2. [为什么选择 Liquibase](#2-为什么选择-liquibase)
3. [快速开始](#3-快速开始)
4. [核心概念详解](#4-核心概念详解)
5. [变更类型 (Change Types)](#5-变更类型-change-types)
6. [实战项目：Spring Boot 集成](#6-实战项目spring-boot-集成)
7. [高级特性](#7-高级特性)
8. [生产环境最佳实践](#8-生产环境最佳实践)
9. [故障排查与监控](#9-故障排查与监控)
10. [扩展与集成](#10-扩展与集成)

---

## 1. 什么是 Liquibase

### 1.1 简介
Liquibase 是一个开源的数据库版本控制工具，支持多种数据库（MySQL, PostgreSQL, Oracle, SQL Server 等），能够跟踪、管理和应用数据库 schema 的变更。

### 1.2 核心价值
- **数据库版本控制**：将数据库变更纳入版本控制系统
- **跨数据库支持**：同一套变更脚本支持多种数据库
- **幂等执行**：安全地重复执行，不产生重复数据
- **回滚支持**：支持多种回滚策略
- **增量更新**：只执行未应用的变更

---

## 2. 为什么选择 Liquibase

### 2.1 与 Flyway 对比

| 特性 | Liquibase | Flyway |
|------|-----------|--------|
| 变更格式 | XML/YAML/SQL/JSON | SQL only |
| 回滚支持 | 原生支持 | 有限 |
| 变更集概念 | 原子性分组 | 单文件 |
| 学习曲线 | 较陡 | 简单 |
| 社区活跃度 | 高 | 高 |
| Spring Boot 集成 | 官方支持 | 官方支持 |

### 2.2 使用场景
- 多环境数据库部署（开发、测试、生产）
- 微服务架构中的数据库管理
- 需要回滚能力的持续集成/持续部署（CI/CD）
- 团队协作开发

---

## 3. 快速开始

### 3.1 安装方式

#### 3.1.1 命令行安装（推荐用于学习）
```bash
# macOS
brew install liquibase

# 下载 JAR 包
wget https://github.com/liquibase/liquibase/releases/download/v4.25.0/liquibase-4.25.0.tar.gz
tar -xzf liquibase-4.25.0.tar.gz

# 配置环境变量
export LIQUIBASE_HOME=/path/to/liquibase
export PATH=$PATH:$LIQUIBASE_HOME/bin
```

#### 3.1.2 Maven 集成
```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-core</artifactId>
        <version>4.25.0</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-maven-plugin</artifactId>
            <version>4.25.0</version>
            <configuration>
                <propertyFile>src/main/resources/liquibase.properties</propertyFile>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### 3.1.3 Gradle 集成
```groovy
// build.gradle
dependencies {
    implementation 'org.liquibase:liquibase-core:4.25.0'
}

plugins {
    id 'org.liquibase' version '4.25.0'
}

liquibase {
    activities {
        main {
            changeLogFile 'src/main/resources/db/changelog/db.changelog-master.yaml'
            url 'jdbc:mysql://localhost:3306/your_database'
            username 'root'
            password 'password'
        }
    }
}
```

### 3.2 第一个变更

#### 3.2.1 创建 changelog 文件
```yaml
# src/main/resources/db/changelog/db.changelog-1.0.yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: developer
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
                    nullable: false
              - column:
                  name: username
                  type: VARCHAR(50)
                  constraints:
                    nullable: false
                    unique: true
              - column:
                  name: email
                  type: VARCHAR(100)
              - column:
                  name: created_at
                  type: TIMESTAMP
                  defaultValueDate: now()
```

#### 3.2.2 创建主日志文件
```yaml
# src/main/resources/db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - include:
      file: db/changelog/db.changelog-1.0.yaml
```

#### 3.2.3 配置文件
```properties
# src/main/resources/liquibase.properties
url=jdbc:mysql://localhost:3306/your_database
username=root
password=password
driver=com.mysql.cj.jdbc.Driver
changeLogFile=db/changelog/db.changelog-master.yaml
```

#### 3.2.4 执行变更
```bash
# Maven
mvn liquibase:update

# 命令行
liquibase update
```

---

## 4. 核心概念详解

### 4.1 ChangeLog（变更日志）
ChangeLog 是 Liquibase 的核心文件，定义了所有数据库变更的集合。

#### 4.1.1 目录结构示例
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml
├── db.changelog-1.0.yaml
├── db.changelog-1.1.yaml
├── db.changelog-1.2.yaml
└── tables/
    ├── users.yaml
    └── orders.yaml
```

#### 4.1.2 嵌套 include
```yaml
# db.changelog-master.yaml
databaseChangeLog:
  - includeAll:
      path: db/changelog/tables/
  - include:
      file: db/changelog/db.changelog-1.1.yaml
      relativeToChangelogFile: true
```

### 4.2 ChangeSet（变更集）
ChangeSet 是 Liquibase 执行变更的基本单位，具有以下特性：
- **唯一标识**：由 id + author 唯一标识
- **幂等性**：默认按 id + author + checksum 去重
- **原子性**：一个 ChangeSet 中的所有变更要么全部成功，要么全部回滚

```yaml
- changeSet:
    id: 2
    author: developer
    failOnError: true  # 失败时是否停止
    runAlways: false   # 每次都执行
    runOrder: early    # 执行顺序
    comments: "Add user profile table"
    changes:
      - createTable:
          tableName: user_profiles
```

### 4.3 Contexts（上下文）
用于控制变更在不同环境中的执行：

```yaml
- changeSet:
    id: 3
    author: developer
    context: dev  # 只在 dev 环境执行
    changes:
      - sql:
          sql: INSERT INTO users (username) VALUES ('dev_user');
```

```bash
mvn liquibase:update -Dliquibase.contexts=dev,test
```

### 4.4 Labels（标签）
更灵活的变更控制方式：

```yaml
- changeSet:
    id: 4
    author: developer
    labels: production-safe
    changes:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: last_login
                type: TIMESTAMP
```

```bash
mvn liquibase:update -Dliquibase.labels=production-safe
```

### 4.5 Checksum（校验和）
Liquibase 使用 MD5 校验和来检测变更：
- 首次执行：记录校验和
- 再次执行：比较校验和
- 变更检测：如果校验和不同，会抛出异常

---

## 5. 变更类型 (Change Types)

### 5.1 表操作

#### 5.1.1 创建表
```yaml
- changeSet:
    id: create-users-table
    author: developer
    changes:
      - createTable:
          tableName: users
          remarks: "用户表"
          columns:
            - column:
                name: id
                type: BIGINT
                autoIncrement: true
                constraints:
                  primaryKey: true
                  nullable: false
            - column:
                name: username
                type: VARCHAR(50)
                constraints:
                  nullable: false
                  unique: true
            - column:
                name: email
                type: VARCHAR(100)
                constraints:
                  nullable: false
```

#### 5.1.2 添加列
```yaml
- changeSet:
    id: add-user-phone
    author: developer
    changes:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: phone
                type: VARCHAR(20)
                defaultValue: null
            - column:
                name: status
                type: INT
                defaultValueNumeric: 1
```

#### 5.1.3 修改列
```yaml
- changeSet:
    id: modify-column
    author: developer
    changes:
      - modifyDataType:
          tableName: users
          columnName: email
          newDataType: VARCHAR(200)
```

#### 5.1.4 删除列
```yaml
- changeSet:
    id: remove-column
    author: developer
    changes:
      - dropColumn:
          tableName: users
          columnName: temporary_field
```

#### 5.1.5 重命名表/列
```yaml
- changeSet:
    id: rename-table
    author: developer
    changes:
      - renameTable:
          oldTableName: user_accounts
          newTableName: users

- changeSet:
    id: rename-column
    author: developer
    changes:
      - renameColumn:
          tableName: users
          oldColumnName: user_name
          newColumnName: username
```

### 5.2 索引操作

```yaml
- changeSet:
    id: create-indexes
    author: developer
    changes:
      - createIndex:
          tableName: users
          indexName: idx_users_email
          columns:
            - column:
                name: email
          unique: true
          tablespace: USERS
```

### 5.3 约束操作

```yaml
- changeSet:
    id: add-constraints
    author: developer
    changes:
      - addUniqueConstraint:
          tableName: users
          columnNames: email
          constraintName: uk_users_email
          deferrable: true
          initiallyDeferred: true

      - addForeignKeyConstraint:
          baseTableName: orders
          baseColumnNames: user_id
          constraintName: fk_orders_user
          referencedTableName: users
          referencedColumnNames: id
          onDelete: CASCADE
          onUpdate: RESTRICT
```

### 5.4 原始 SQL

```yaml
- changeSet:
    id: raw-sql-example
    author: developer
    changes:
      - sql:
          sql: |
            INSERT INTO users (username, email) VALUES
            ('user1', 'user1@example.com'),
            ('user2', 'user2@example.com');
          stripComments: false
          splitStatements: true
```

### 5.5 数据操作

```yaml
- changeSet:
    id: insert-data
    author: developer
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

      - loadData:
          tableName: users
          file: config/users.csv
          separator: ,
          quotchar: '"'

      - loadUpdateData:
          tableName: configuration
          file: config/config.csv
          primaryKey: config_key
```

### 5.6 回滚操作

#### 5.6.1 自动回滚
```yaml
- changeSet:
    id: with-auto-rollback
    author: developer
    changes:
      - createTable:
          tableName: temp_table
      - addColumn:
          tableName: users
          columns:
            - column:
                name: temp_field
                  type: VARCHAR(50)
    rollback: |
      DROP TABLE temp_table;
```

#### 5.6.2 显式回滚
```yaml
- changeSet:
    id: with-explicit-rollback
    author: developer
    changes:
      - sql:
          sql: UPDATE users SET status = 1 WHERE id > 1000
    rollback: |
      UPDATE users SET status = 0 WHERE id > 1000;
```

#### 5.6.3 回滚标签
```bash
# 回滚到指定标签
liquibase rollback v1.0

# 回滚指定数量的变更集
liquibase rollbackCount 3

# 回滚到指定日期
liquibase rollbackToDate 2024-01-01
```

---

## 6. 实战项目：Spring Boot 集成

### 6.1 添加依赖

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-core</artifactId>
    </dependency>
    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 6.2 配置文件

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/myapp
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
    contexts: dev,test,prod
    default-schema: public
    drop-first: false
    should-run: true
    labels: production-safe
    parameters:
      table.prefix: app_
      schema.name: public
```

### 6.3 完整的项目结构

```
src/main/resources/
├── application.yml
└── db/
    ├── changelog/
    │   ├── db.changelog-master.yaml
    │   ├── db.changelog-1.0.yaml
    │   ├── db.changelog-1.1.yaml
    │   └── tables/
    │       ├── users.yaml
    │       ├── orders.yaml
    │       └── products.yaml
    └── master.xml
```

### 6.4 Spring Boot 自动配置

Spring Boot 会自动配置 `LiquibaseProperties`，并创建 `SpringLiquibase` Bean：

```java
@Configuration
public class LiquibaseConfig {
    
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isShouldRun());
        
        // 添加自定义配置
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:db/**/*.yaml");
            liquibase.setResourceLoader(new SpringResourceLoader(resources));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Liquibase resources", e);
        }
        
        return liquibase;
    }
}
```

### 6.5 执行时机控制

```java
@Component
public class LiquibaseStartupRunner implements ApplicationRunner {
    
    private final SpringLiquibase liquibase;
    
    public LiquibaseStartupRunner(SpringLiquibase liquibase) {
        this.liquibase = liquibase;
    }
    
    @Override
    public void run(ApplicationArguments args) {
        // 在应用启动后执行 Liquibase 更新
        // Spring Boot 默认会自动执行，此处可用于自定义逻辑
    }
}
```

### 6.6 禁用/启用 Liquibase

```yaml
spring:
  liquibase:
    enabled: false  # 禁用自动执行
```

```java
@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 7. 高级特性

### 7.1 预置条件 (Preconditions)

```yaml
- changeSet:
    id: conditional-change
    author: developer
    preconditions:
      - precondition:
          onFail: HALT  # HALT, WARN, CONTINUE
          onError: HALT
          os: Windows 10  # 操作系统条件
          dbms: mysql     # 数据库条件
          tableExists:
            tableName: users
            
    changes:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: new_column
                type: VARCHAR(100)
```

### 7.2 自定义变更 (Custom Changes)

```java
package com.example.liquibase;

import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.sql.PreparedStatement;

public class CustomDataMigrationChange implements CustomTaskChange {
    
    private String tableName;
    private String columnName;
    
    @Override
    public void setUp() {
        // 初始化配置
    }
    
    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // 设置文件访问器
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = new ValidationErrors();
        if (tableName == null) {
            errors.addError("tableName is required");
        }
        return errors;
    }
    
    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection connection = (JdbcConnection) database.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE " + tableName + " SET " + columnName + " = ?")) {
            stmt.setString(1, "migrated_value");
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new CustomChangeException("Failed to migrate data", e);
        }
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Custom data migration completed for " + tableName;
    }
    
    @Override
    public void undo(Database database) throws CustomChangeException {
        // 回滚逻辑
    }
    
    // Getters and Setters
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
```

### 7.3 增量数据加载

```yaml
- changeSet:
    id: incremental-load
    author: developer
    changes:
      - loadUpdateData:
          tableName: products
          file: data/products.csv
          primaryKey: product_id
          onlyUpdate: true
          catalogName: shop
```

### 7.4 存储过程和函数

```yaml
- changeSet:
    id: create-stored-procedure
    author: developer
    changes:
      - sql:
          sql: |
            DELIMITER //
            CREATE PROCEDURE calculate_total(IN userId BIGINT)
            BEGIN
                SELECT SUM(amount) INTO @total
                FROM orders
                WHERE user_id = userId AND status = 'completed';
                SELECT @total AS total_amount;
            END //
            DELIMITER ;
          stripComments: false
```

### 7.5 触发器管理

```yaml
- changeSet:
    id: create-audit-trigger
    author: developer
    changes:
      - sql:
          sql: |
            CREATE TRIGGER users_audit_trigger
            AFTER INSERT ON users
            FOR EACH ROW
            BEGIN
                INSERT INTO audit_log (action, table_name, record_id, created_at)
                VALUES ('INSERT', 'users', NEW.id, NOW());
            END;
```

### 7.6 视图管理

```yaml
- changeSet:
    id: create-views
    author: developer
    changes:
      - createView:
          viewName: user_summary
          replaceIfExists: true
          sql: |
            SELECT u.id, u.username, COUNT(o.id) as order_count
            FROM users u
            LEFT JOIN orders o ON u.id = o.user_id
            GROUP BY u.id, u.username
```

---

## 8. 生产环境最佳实践

### 8.1 安全最佳实践

#### 8.1.1 敏感信息管理
```yaml
# 不要在配置文件中明文存储密码
spring:
  datasource:
    url: jdbc:mysql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

```properties
# liquibase.properties - 使用环境变量
url=${LIQUIBASE_DB_URL}
username=${LIQUIBASE_DB_USERNAME}
password=${LIQUIBASE_DB_PASSWORD}
```

#### 8.1.2 SSL/TLS 连接
```properties
url=jdbc:mysql://localhost:3306/database?useSSL=true&requireSSL=true&verifyServerCertificate=true
```

### 8.2 性能优化

#### 8.2.1 并发控制
```yaml
spring:
  liquibase:
    liquibase-schema: public
    liquibase-catalog: database_name
```

#### 8.2.2 批量执行
```java
@Configuration
public class LiquibaseConfig {
    
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/master.yaml");
        liquibase.setContexts("prod");
        liquibase.setDefaultSchema("public");
        liquibase.setDropFirst(false);
        liquibase.setShouldRun(true);
        
        // 优化配置
        Properties props = new Properties();
        props.setProperty("liquibase.database.class", 
            "liquibase.database.core.PostgresDatabase");
        props.setProperty("liquibase.hub.mode", "off");
        
        return liquibase;
    }
}
```

### 8.3 高可用部署

#### 8.3.1 锁管理
```sql
-- 查看 Liquibase 锁状态
SELECT * FROM DATABASECHANGELOGLOCK;

-- 手动释放锁（仅在异常情况下）
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE, LOCKGRANTED = NULL, LOCKEDBY = NULL WHERE ID = 1;
```

#### 8.3.2 避免并发执行
```bash
# 使用锁文件
touch /tmp/liquibase.lock

# 在 CI/CD 中确保单次执行
# 使用分布式锁（Redis, ZooKeeper 等）
```

### 8.4 变更管理策略

#### 8.4.1 版本命名规范
```
V{MAJOR}_{MINOR}_{PATCH}__{YYYYMMDD}__description
示例：V1_2_0__20240121__add_user_profile
```

#### 8.4.2 变更集大小控制
- 单个变更集不超过 50 行 YAML/SQL
- 相关的 DDL 和 DML 放在同一个变更集
- 大型数据迁移拆分为多个变更集

#### 8.4.3 变更顺序管理
```yaml
# 使用序号前缀控制执行顺序
- changeSet:
    id: 001__create-users-table
    author: developer
    changes:
      - createTable:
          tableName: users

- changeSet:
    id: 002__create-orders-table
    author: developer
    changes:
      - createTable:
          tableName: orders
```

### 8.5 测试策略

#### 8.5.1 测试环境配置
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test_database
    username: test
    password: test_password
  
  liquibase:
    enabled: true
    contexts: test
    drop-first: true
```

#### 8.5.2 集成测试
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.liquibase.enabled=true",
    "spring.liquibase.contexts=test"
})
class LiquibaseIntegrationTest {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    @Commit
    void testDatabaseMigration() {
        // 验证表存在
        assertTrue(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_name = 'users'", Integer.class) > 0);
    }
}
```

#### 8.5.3 回滚测试
```java
@Test
void testRollback() throws Exception {
    // 记录当前状态
    int beforeCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM DATABASECHANGELOG", Integer.class);
    
    // 执行回滚
    liquibase.performRollback(beforeCount, "");
    
    // 验证回滚结果
    int afterCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM DATABASECHANGELOG", Integer.class);
    
    assertEquals(beforeCount, afterCount);
}
```

### 8.6 CI/CD 集成

#### 8.6.1 GitLab CI
```yaml
# .gitlab-ci.yml
stages:
  - migrate
  - test
  - deploy

migrate:
  stage: migrate
  image: maven:3.8-openjdk-17
  script:
    - mvn liquibase:update -Dliquibase.contexts=ci
  only:
    - main

migrate:rollback:
  stage: migrate
  image: maven:3.8-openjdk-17
  script:
    - mvn liquibase:rollback -Dliquibase.rollbackTag=previous_release
  when: manual
```

#### 8.6.2 GitHub Actions
```yaml
# .github/workflows/liquibase.yml
name: Liquibase Migration

on:
  push:
    branches: [main]
  workflow_dispatch:
    inputs:
      rollback:
        description: 'Rollback to tag'
        required: false

jobs:
  migrate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run Liquibase
        run: mvn liquibase:update
        env:
          LIQUIBASE_URL: ${{ secrets.LIQUIBASE_URL }}
          LIQUIBASE_USERNAME: ${{ secrets.LIQUIBASE_USERNAME }}
          LIQUIBASE_PASSWORD: ${{ secrets.LIQUIBASE_PASSWORD }}
```

#### 8.6.3 Jenkins Pipeline
```groovy
// Jenkinsfile
pipeline {
    agent any
    
    environment {
        LIQUIBASE_URL = credentials('liquibase-db-url')
        LIQUIBASE_USERNAME = credentials('liquibase-db-username')
        LIQUIBASE_PASSWORD = credentials('liquibase-db-password')
    }
    
    stages {
        stage('Migrate') {
            steps {
                sh '''
                    mvn liquibase:update \
                        -Dliquibase.url=${LIQUIBASE_URL} \
                        -Dliquibase.username=${LIQUIBASE_USERNAME} \
                        -Dliquibase.password=${LIQUIBASE_PASSWORD} \
                        -Dliquibase.contexts=production
                '''
            }
        }
        
        stage('Verify') {
            steps {
                sh '''
                    mvn liquibase:status \
                        -Dliquibase.url=${LIQUIBASE_URL} \
                        -Dliquibase.username=${LIQUIBASE_USERNAME} \
                        -Dliquibase.password=${LIQUIBASE_PASSWORD}
                '''
            }
        }
    }
    
    post {
        failure {
            echo 'Liquibase migration failed!'
        }
        success {
            echo 'Liquibase migration completed successfully!'
        }
    }
}
```

### 8.7 监控与告警

#### 8.7.1 变更日志表监控
```sql
-- 监控未完成的变更
SELECT * FROM DATABASECHANGELOG 
WHERE EXECUTIONDATE IS NULL;

-- 监控失败的变更
SELECT * FROM DATABASECHANGELOG 
WHERE EXECUTIONSUCCESS = 0;

-- 监控变更执行时间
SELECT ID, AUTHOR, MD5SUM, EXECUTIONSECONDS 
FROM DATABASECHANGELOG 
ORDER BY EXECUTIONSECONDS DESC 
LIMIT 10;
```

#### 8.7.2 自定义监控指标
```java
@Component
public class LiquibaseMetrics {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public LiquibaseMetrics(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Scheduled(fixedRate = 60000)
    public void monitorLiquibase() {
        // 待执行变更数
        Integer pendingCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM DATABASECHANGELOG WHERE EXECUTIONDATE IS NULL", 
            Integer.class);
        
        // 执行中状态（通过锁表判断）
        Boolean isLocked = jdbcTemplate.queryForObject(
            "SELECT LOCKED FROM DATABASECHANGELOGLOCK WHERE ID = 1", 
            Boolean.class);
        
        // 发送到监控系统
        if (pendingCount > 0) {
            // 告警：有待执行的变更
        }
        
        if (isLocked) {
            // 告警：Liquibase 正在执行中
        }
    }
}
```

---

## 9. 故障排查与监控

### 9.1 常见问题及解决方案

#### 9.1.1 锁等待超时
```sql
-- 检查锁状态
SELECT * FROM DATABASECHANGELOGLOCK;

-- 解锁（谨慎使用）
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1;
```

#### 9.1.2 校验和不匹配
```bash
# 清除校验和记录
mvn liquibase:clearCheckSums

# 或手动更新
UPDATE DATABASECHANGELOG SET MD5SUM = NULL WHERE ID = 'your-changeset-id';
```

#### 9.1.3 变更集重复执行
```yaml
- changeSet:
    id: 1
    author: developer
    runOnChange: true  # 当校验和变化时重新执行
    changes:
      - createTable:
          tableName: users
```

#### 9.1.4 回滚失败
```bash
# 生成回滚 SQL 而非直接执行
mvn liquibase:rollbackSQL -Dliquibase.rollbackTag=v1.0

# 查看回滚日志
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0 -Dliquibase.verbose=true
```

### 9.2 日志配置

```xml
<!-- logback.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/liquibase.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/liquibase.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="liquibase" level="INFO"/>
    <logger name="liquibase.changelog" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 9.3 详细日志输出

```java
// 获取详细的执行日志
public class DetailedLiquibaseExecutor {
    
    public void executeWithLogging(SpringLiquibase liquibase) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("liquibase").setLevel(Level.DEBUG);
        loggerContext.getLogger("liquibase.sql").setLevel(Level.DEBUG);
        
        liquibase.update("");
    }
}
```

---

## 10. 扩展与集成

### 10.1 自定义扩展开发

```java
// 注册自定义变更类型
package com.example.liquibase.extension;

import liquibase.change.custom.CustomChange;
import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;

public class MyCustomChange implements CustomSqlChange {
    
    private String tableName;
    private String columnName;
    private String defaultValue;
    
    @Override
    public Sql[] generateStatements(Database database) throws CustomChangeException {
        String sql = "ALTER TABLE " + tableName + 
                     " ADD COLUMN " + columnName + 
                     " VARCHAR(100) DEFAULT '" + defaultValue + "'";
        return new Sql[]{new UnparsedSql(sql)};
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Added column " + columnName + " to table " + tableName;
    }
    
    // Getters and Setters
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
```

### 10.2 Spring Boot Starter

```java
// 自动配置类
@Configuration
@ConditionalOnClass(SpringLiquibase.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class LiquibaseAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean(SpringLiquibase.class)
    public SpringLiquibase liquibase(DataSource dataSource, 
                                     Environment env,
                                     ResourceLoader resourceLoader) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(env.getProperty("spring.liquibase.change-log"));
        liquibase.setContexts(env.getProperty("spring.liquibase.contexts"));
        liquibase.setDefaultSchema(env.getProperty("spring.liquibase.default-schema"));
        liquibase.setDropFirst(Boolean.parseBoolean(
            env.getProperty("spring.liquibase.drop-first", "false")));
        liquibase.setShouldRun(Boolean.parseBoolean(
            env.getProperty("spring.liquibase.enabled", "true")));
        return liquibase;
    }
}
```

### 10.3 与 Spring Cloud 集成

```java
// 使用 Spring Cloud Config 管理配置
@SpringCloudApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// 配置中心
// configserver/liquibase.yml
spring:
  liquibase:
    change-log: classpath:db/changelog/master.yaml
    contexts: ${spring.profiles.active}
```

### 10.4 与数据流集成

```yaml
# 在数据管道中使用 Liquibase
- stage: database_migration
  steps:
    - name: Liquibase Migration
      image: liquibase/liquibase:latest
      env:
        LIQUIBASE_URL: ${{ secrets.DB_URL }}
        LIQUIBASE_USERNAME: ${{ secrets.DB_USER }}
        LIQUIBASE_PASSWORD: ${{ secrets.DB_PASSWORD }}
      script:
        - liquibase --changeLogFile=db/changelog/master.yaml update
```

---

## 附录

### A. 命令速查表

| 命令 | 描述 |
|------|------|
| `update` | 执行所有待应用的变更 |
| `updateSQL` | 生成待执行 SQL 而不执行 |
| `updateToTag <tag>` | 更新到指定标签 |
| `rollback <tag>` | 回滚到指定标签 |
| `rollbackSQL <tag>` | 生成回滚 SQL |
| `rollbackCount <n>` | 回滚 n 个变更集 |
| `rollbackDate <date>` | 回滚到指定日期 |
| `status` | 查看待执行变更 |
| `validate` | 验证所有变更集 |
| `clearCheckSums` | 清除校验和 |
| `diff` | 对比当前数据库和参考数据库 |

### B. 推荐的目录结构

```
src/main/resources/
├── db/
│   ├── changelog/
│   │   ├── master.yaml
│   │   ├── V1_0_0/
│   │   │   ├── 001__create_users_table.yaml
│   │   │   └── 002__create_orders_table.yaml
│   │   ├── V1_1_0/
│   │   │   └── 001__add_user_profile.yaml
│   │   └── V1_2_0/
│   │       └── 001__add_order_history.yaml
│   ├── data/
│   │   ├── users.csv
│   │   └── products.csv
│   └── scripts/
│       ├── postgresql/
│       │   └── functions.sql
│       └── mysql/
│           └── procedures.sql
└── liquibase.properties
```

### C. 资源链接

- **官方文档**：https://docs.liquibase.com/
- **GitHub 仓库**：https://github.com/liquibase/liquibase
- **变更类型参考**：https://docs.liquibase.com/change-types.html
- **社区论坛**：https://forum.liquibase.org/
- **示例项目**：https://github.com/liquibase/liquibase-examples

---

## 学习路径建议

1. **第一周**：理解核心概念，创建第一个 changelog
2. **第二周**：实践各种变更类型，熟悉 XML/YAML/SQL 格式
3. **第三周**：Spring Boot 集成，CI/CD 配置
4. **第四周**：生产环境部署，监控，故障排查
5. **持续学习**：关注官方文档更新，参与社区讨论

**建议**：在实际项目中应用所学知识，实践是最好的学习方式！

---

*文档版本：1.0*  
*最后更新：2024年1月*  
*作者：Liquibase 学习指南*
