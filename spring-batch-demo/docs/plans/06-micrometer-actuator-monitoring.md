# 实现计划 V6：Micrometer + Actuator 替换旧监控

## 1. 概述

用 Spring Boot Actuator + Micrometer 替换现有自定义监控组件，并增强监控能力。

**五个子方案：**
1. LoggingMeterRegistry 每 20s 输出指标快照到独立日志文件
2. 删除旧监控组件（ItemTimingListener 等 6 个文件）
3. ChunkCommitTracker — 统计每个 chunk 全间隔耗时（含 commit 阶段）
4. BatchMetricsEndpoint — `/actuator/batch-metrics` 一次查询聚合全貌
5. FileTypeMeterFilter — 给所有 `spring.batch.*` 指标加 `fileType` 标签

## 2. 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 与现有监控关系 | 替换（A 方案） | 统一用 Actuator，删除自写组件 |
| LoggingMeterRegistry | 独立 `logs/metrics/metrics.log`，每 20s dump | 与业务日志分离，方便回顾 |
| ChunkCommitTracker 测量范围 | chunk 全间隔（上一个 afterChunk 到当前 afterChunk） | 最简单，一个墙钟差值，完整覆盖 read+process+write+commit |
| 自定义指标 | Counter（skip）+ Gauge（filter）+ itemsPerSec | 补齐内置 Timer 不覆盖的部分 |
| fileType 标签 | MeterFilter 注入 | 将 tableType 维度加入 Actuator 指标查询 |
| JSON 日志持久化 | 删除（由 LoggingMeterRegistry 替代） | 统一到 Micrometer 体系 |

## 3. 改动清单

### 3.1 pom.xml（已完成）

已添加 `spring-boot-starter-actuator`。

### 3.2 application.properties（已完成）

已配置 `management.endpoints.web.exposure.include=metrics,health` 和 `spring.batch.observation.enabled=true`。

### 3.3 新增：MonitoringConfig（LoggingMeterRegistry）

**新建** `config/MonitoringConfig.java`

```java
package cn.reid.springbatchdemo.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MonitoringConfig {

    @Bean
    public LoggingMeterRegistry loggingMeterRegistry() {
        return new LoggingMeterRegistry(new LoggingRegistryConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(20);
            }

            @Override
            public String get(String key) {
                return null;
            }
        }, Clock.SYSTEM);
    }
}
```

每 20s 输出一行到日志，类似：
```
[metrics] spring.batch.item.read: count=450, total=12.34s, max=56ms
[metrics] spring.batch.item.process: count=450, total=0.23s
[metrics] spring.batch.chunk.write: count=10, total=0.45s
[metrics] jvm.memory.used: 512MB
[metrics] hikaricp.connections.active: 2
```

### 3.4 新增：logback 配置（metrics 独立文件）

在 `logback-spring.xml` 中新增：

```xml
<appender name="METRICS_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_PATH:-./logs}/metrics/metrics.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
        <fileNamePattern>${LOG_PATH:-./logs}/metrics/metrics.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxFileSize>2MB</maxFileSize>
        <maxHistory>30</maxHistory>
        <totalSizeCap>1GB</totalSizeCap>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>

<logger name="io.micrometer.core.instrument.logging.LoggingMeterRegistry" level="INFO" additivity="false">
    <appender-ref ref="METRICS_FILE"/>
</logger>
```

删除旧的 `MONITOR_FILE` appender 和 `<logger name="MonitorLogger">` / `<logger name="ChunkMonitorLogger">`。

### 3.5 新增：ChunkCommitTracker

**新建** `monitor/ChunkCommitTracker.java`

```java
package cn.reid.springbatchdemo.monitor;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkCommitTracker implements ChunkListener {

    private final AtomicLong chunkCount = new AtomicLong();
    private final AtomicLong totalChunkNs = new AtomicLong();
    private final AtomicLong maxChunkNs = new AtomicLong();
    private volatile long lastChunkEndNs = System.nanoTime();

    // 用于取 p95 近似：维护最近 100 个 chunk 耗时
    private final long[] recentDurations = new long[100];
    private final AtomicInteger index = new AtomicInteger();

    @Override
    public void beforeChunk(ChunkContext context) {
        // 不需要特殊处理
    }

    @Override
    public void afterChunk(ChunkContext context) {
        long now = System.nanoTime();
        long elapsed = now - lastChunkEndNs;
        lastChunkEndNs = now;

        if (chunkCount.get() > 0) {
            totalChunkNs.addAndGet(elapsed);
            updateMax(elapsed);
            recordRecent(elapsed);
        }
        chunkCount.incrementAndGet();
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        long now = System.nanoTime();
        lastChunkEndNs = now;
        chunkCount.incrementAndGet();
    }

    public long getChunkCount()      { return chunkCount.get(); }
    public long getTotalChunkMs()    { return totalChunkNs.get() / 1_000_000; }
    public long getMaxChunkMs()      { return maxChunkNs.get() / 1_000_000; }
    public double getAvgChunkMs() {
        long count = chunkCount.get();
        return count > 0 ? getTotalChunkMs() / (double) count : 0;
    }
    public long[] getRecentDurations() { return recentDurations; }

    private void updateMax(long value) {
        maxChunkNs.updateAndGet(v -> Math.max(v, value));
    }

    private void recordRecent(long value) {
        recentDurations[index.getAndIncrement() % 100] = value;
    }
}
```

在 StepConfig 中注册：`.listener(chunkCommitTracker)`。

### 3.6 新增：FileTypeMeterFilter

**新建** `monitor/FileTypeMeterFilter.java`

```java
package cn.reid.springbatchdemo.monitor;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.stereotype.Component;

@Component
public class FileTypeMeterFilter implements MeterFilter {

    private static final ThreadLocal<String> currentFileType = new ThreadLocal<>();

    public static void setFileType(String fileType) {
        currentFileType.set(fileType);
    }

    public static void clear() {
        currentFileType.remove();
    }

    @Override
    public Meter.Id map(Meter.Id id) {
        String fileType = currentFileType.get();
        if (fileType != null && id.getName().startsWith("spring.batch.")) {
            return id.withTag(Tag.of("fileType", fileType));
        }
        return id;
    }
}
```

在 `beforeStep()` 中调用 `FileTypeMeterFilter.setFileType(fileType)`，`afterStep()` 中调用 `clear()`。

### 3.7 新增：BatchMetricsEndpoint

**新建** `monitor/BatchMetricsEndpoint.java`

```java
package cn.reid.springbatchdemo.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@WebEndpoint(id = "batch-metrics")
public class BatchMetricsEndpoint {

    private final MeterRegistry meterRegistry;
    private final SkipCollectorListener skipCollector;
    private final ChunkCommitTracker chunkTracker;

    public BatchMetricsEndpoint(MeterRegistry meterRegistry,
                                SkipCollectorListener skipCollector,
                                ChunkCommitTracker chunkTracker) {
        this.meterRegistry = meterRegistry;
        this.skipCollector = skipCollector;
        this.chunkTracker = chunkTracker;
    }

    @ReadOperation
    public Map<String, Object> metrics() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("read", timerData("spring.batch.item.read"));
        result.put("process", timerData("spring.batch.item.process"));
        result.put("write", timerData("spring.batch.chunk.write"));

        // chunk 聚合
        Map<String, Object> chunk = new LinkedHashMap<>();
        chunk.put("count", chunkTracker.getChunkCount());
        chunk.put("avgMs", Math.round(chunkTracker.getAvgChunkMs() * 10) / 10.0);
        chunk.put("maxMs", chunkTracker.getMaxChunkMs());
        result.put("chunk", chunk);

        // skip
        Map<String, Object> skips = new LinkedHashMap<>();
        skips.put("total", skipCollector.getTotalSkips());
        skips.put("reasons", skipCollector.getSummary());
        result.put("skips", skips);

        return result;
    }

    private Map<String, Object> timerData(String name) {
        Map<String, Object> data = new LinkedHashMap<>();
        Timer t = meterRegistry.find(name).timer();
        if (t != null) {
            data.put("count", t.count());
            data.put("totalMs", t.totalTime(TimeUnit.MILLISECONDS));
            data.put("maxMs", t.max(TimeUnit.MILLISECONDS));
            data.put("meanMs", Math.round(t.mean(TimeUnit.MILLISECONDS) * 10) / 10.0);
        }
        return data;
    }
}
```

### 3.8 新增：BatchMetricsConfig（自定义 Counter + Gauge）

**新建** `monitor/BatchMetricsConfig.java`

```java
package cn.reid.springbatchdemo.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchMetricsConfig {

    @Bean
    public MeterBinder batchMetrics() {
        return registry -> {
            Counter.builder("batch.skip")
                .description("Total skipped items grouped by reason")
                .register(registry);

            Counter.builder("batch.filter")
                .description("Total items filtered out by processor")
                .register(registry);
        };
    }
}
```

### 3.9 改造 SkipCollectorListener

保留 `SkipCollectorListener` 但注入 `MeterRegistry`，skip 时同时 increment Counter：

```java
private void record(String reason) {
    if (enabled) {
        skipReasons.computeIfAbsent(reason, k -> new AtomicInteger()).incrementAndGet();
        registry.counter("batch.skip", "reason", reason).increment();
    }
}
```

### 3.10 删除旧文件（共 6 个）

| 文件 | 理由 |
|------|------|
| `monitor/ItemTimingListener.java` | 计时由 `spring.batch.item.*` Timer 替代 |
| `monitor/ChunkMetricsChunkListener.java` | 依赖 ItemTimingListener，由 ChunkCommitTracker 替代 |
| `monitor/ChunkMonitorLogger.java` | 逐 chunk 日志不再需要 |
| `monitor/MonitorLogger.java` | Step 日志由 LoggingMeterRegistry + `spring.batch.step` 替代 |
| `monitor/MonitoringFacade.java` | 门面不再需要 |
| `listener/FileProcessingMetricsListener.java` | StepListener 不再需要 |

### 3.11 更新 StepConfig（共 6 个）

每个 StepConfig 中：
- 删除 `ItemTimingListener timingListener` 参数及 3 行 `.listener()` 注册
- 删除 `MonitoringFacade monitoringFacade` 参数
- 删除 `FileProcessingMetricsListener listener` 参数
- 删除 `new ChunkMetricsChunkListener(...)` 行
- 注入 `ChunkCommitTracker` 并添加 `.listener(chunkCommitTracker)`
- 保留 `.listener(skipListener)`（已对接 MeterRegistry）
- 在 StepExecutionListener 中调用 `FileTypeMeterFilter.setFileType/clear`

### 3.12 更新 logback-spring.xml

- 删除 `MONITOR_FILE` appender
- 删除 `CHUNK_MONITOR_FILE` appender
- 删除 `<logger name="MonitorLogger">` 和 `<logger name="ChunkMonitorLogger">`
- 新增 `METRICS_FILE` appender + `<logger name="io.micrometer.core.instrument.logging.LoggingMeterRegistry">`

### 3.13 更新 CLAUDE.md

- Architecture 章节：`listener/` 路径删除，`monitor/` 下替换为新的文件列表
- Batch Conventions 追加 LoggingMeterRegistry 和 ChunkCommitTracker 的说明

## 4. 使用方式

### 4.1 内置指标（HTTP 实时查询）

```bash
# 读耗时 per-item 分布
curl /actuator/metrics/spring.batch.item.read

# 处理耗时
curl /actuator/metrics/spring.batch.item.process

# chunk 写入耗时
curl /actuator/metrics/spring.batch.chunk.write

# Step 总耗时
curl /actuator/metrics/spring.batch.step

# 以上均支持 fileType 过滤
curl '/actuator/metrics/spring.batch.item.read?tag=fileType:student'
```

### 4.2 聚合端点（一次查询全貌）

```bash
curl /actuator/batch-metrics
```

返回：
```json
{
  "read":    { "count":450, "totalMs":12340, "maxMs":56, "meanMs":27.4 },
  "process": { "count":450, "totalMs":230, "maxMs":8, "meanMs":0.5 },
  "write":   { "count":450, "totalMs":4500, "maxMs":62, "meanMs":10.0 },
  "chunk":   { "count":10, "avgMs":315.0, "maxMs":1890 },
  "skips":   { "total":3, "reasons":{ "FlatFileParseException:字段数不匹配":2, "IllegalArgumentException:非法 gender":1 } }
}
```

### 4.3 日志历史查询（20s 快照）

```bash
tail -f logs/metrics/metrics.log
```

每 20s 输出一行：
```
2025-06-17 12:00:00.000 [thread] INFO i.m.c.i.l.LoggingMeterRegistry - spring.batch.item.read: count=450, total=12.34s, max=56ms
2025-06-17 12:00:00.000 [thread] INFO i.m.c.i.l.LoggingMeterRegistry - spring.batch.item.process: count=450, total=0.23s
2025-06-17 12:00:00.000 [thread] INFO i.m.c.i.l.LoggingMeterRegistry - spring.batch.chunk.write: count=10, total=0.45s
2025-06-17 12:00:00.000 [thread] INFO i.m.c.i.l.LoggingMeterRegistry - jvm.memory.used: 512MB
2025-06-17 12:00:00.000 [thread] INFO i.m.c.i.l.LoggingMeterRegistry - hikaricp.connections.active: 2
```

### 4.4 自定义指标

```bash
# Skip 按原因
curl /actuator/metrics/batch.skip

# Filter 计数
curl /actuator/metrics/batch.filter
```

## 5. 文件变更总表

| 操作 | 文件 |
|------|------|
| 新 | `config/MonitoringConfig.java` |
| 新 | `monitor/ChunkCommitTracker.java` |
| 新 | `monitor/FileTypeMeterFilter.java` |
| 新 | `monitor/BatchMetricsEndpoint.java` |
| 新 | `monitor/BatchMetricsConfig.java` |
| 改 | `monitor/SkipCollectorListener.java` |
| 改 | `FileJobConfig.java` + 5 个 StepConfig |
| 改 | `logback-spring.xml` |
| 改 | `CLAUDE.md` |
| 删 | `monitor/ItemTimingListener.java` |
| 删 | `monitor/ChunkMetricsChunkListener.java` |
| 删 | `monitor/ChunkMonitorLogger.java` |
| 删 | `monitor/MonitorLogger.java` |
| 删 | `monitor/MonitoringFacade.java` |
| 删 | `listener/FileProcessingMetricsListener.java` |

## 6. 验证清单

| 验证项 | 方法 |
|--------|------|
| 编译通过 | `./mvnw clean compile` |
| 测试全部通过 | `./mvnw test` |
| `/actuator/health` 返回 UP | `curl /actuator/health` |
| 内置 batch 指标出现 | `curl /actuator/metrics | grep spring.batch.` |
| `/actuator/batch-metrics` 有数据 | 执行 job 后 `curl /actuator/batch-metrics` |
| 自定义 batch.skip 出现 | `curl /actuator/metrics/batch.skip` |
| fileType 标签生效 | `curl '/actuator/metrics/spring.batch.item.read?tag=fileType:student'` |
| metrics.log 每 20s 写入 | `tail -f logs/metrics/metrics.log` 观察 |
