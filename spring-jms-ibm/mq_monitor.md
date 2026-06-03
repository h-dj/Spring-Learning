下面给你一份可以直接用于**评审 / 立项 / 架构设计文档**的《银行级 MQ 可观测性技术方案（可落地版）》。

我会按“银行可接受 + 可实施 + 可扩展”的标准来写，而不是停留在概念层。

---

# 🧭 银行级 MQ 可观测性技术方案（完整可落地版）

---

# 一、背景与问题定义

当前系统架构：

```text id="b1"
A系统
  ↓
MQ AVQ（Avaloq侧）
  ↓
MQ Bridge（XMITQ + Channel）
  ↓
MQ WAVQ（Avaloq侧）
  ↓
IN Queue（selector: JMSCorrelationID = JMSMessageID）
  ↓
Spring JMS Consumer
  ↓
Grafana埋点
```

---

## ❗ 当前问题

1. IN queue 出现固定 backlog（如 171 条）
2. LPUTTIME / LGETTIME 正常变化
3. 消息未消费但无报错
4. Bridge 正常运行但业务卡住
5. selector / correlation / bridge 三者耦合不可观测

---

## ❗ 根因本质

> MQ 已经“在运行”，但业务“不可观测”

---

# 二、设计目标

## 🎯 总体目标

建立 MQ 全链路可观测体系，实现：

> **消息从生产 → MQ → Bridge → 消费 → 业务完成 全生命周期可追踪**

---

## 🎯 三大能力目标

### 1️⃣ MQ健康可视化

* 队列深度
* Channel状态
* Bridge延迟

---

### 2️⃣ 消息级追踪能力

* messageId / businessId
* correlationId
* selector匹配情况

---

### 3️⃣ 消费闭环验证

* 是否真正消费
* 是否业务完成
* 是否卡在 selector / bridge / consumer

---

# 三、总体架构设计

---

```text id="a1"
                ┌─────────────────────┐
                │   Grafana Dashboard │
                └────────┬────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
┌───────▼──────┐ ┌───────▼──────┐ ┌──────▼────────┐
│ MQ Exporter  │ │ MQ Diagnose  │ │ Trace System  │
│ (Prometheus) │ │ API Service  │ │ (Kafka/ES)    │
└───────┬──────┘ └───────┬──────┘ └──────┬────────┘
        │                │                │
        └────────┬───────┴───────┬────────┘
                 │               │
        ┌────────▼──────┐  ┌────▼─────────┐
        │ MQ AVQ        │  │ MQ WAVQ      │
        │ + XMITQ       │  │ + IN Queue   │
        └────────┬──────┘  └────┬─────────┘
                 │              │
                 └──── MQ Bridge ────┘
```

---

# 四、核心模块设计

---

# 🧩 1. MQ基础监控层（MQ Exporter）

---

## 🎯 目标

监控 MQ 是否“活着 + 正常传输”

---

## 📊 采集指标

### Queue指标

* CURDEPTH
* IPPROCS
* OPPROCS
* LPUTTIME / LGETTIME

---

### Channel指标（Bridge关键）

* CHANNEL STATUS
* MSGS SENT / RECEIVED
* RETRY COUNT
* XMITQ DEPTH

---

### XMITQ指标（关键）

* backlog
* oldest message age

---

## 📌 技术实现

* Prometheus mq exporter
* IBM MQ metrics plugin
* 或 JMX/REST polling

---

## 📈 输出

* Queue健康面板
* Bridge健康面板

---

# 🧩 2. MQ诊断API层（核心能力）

---

## 🎯 目标

解决你现在最大痛点：

> “171条为什么不动？”

---

## 📌 API设计

### ✔ 队列诊断API

```http id="d1"
GET /mq/diagnose/queue?name=IN_QUEUE
```

---

### 返回：

```json id="d2"
{
  "queueManager": "WAVQ",
  "queueName": "IN_QUEUE",
  "depth": 171,

  "oldestMessageAgeSeconds": 7200,

  "messageSample": [
    {
      "msgId": "ID:12345",
      "correlationId": "ID:67890",
      "putTime": "2026-06-04T10:00:00"
    }
  ],

  "selectorAnalysis": {
    "selector": "JMSCorrelationID = JMSMessageID",
    "matchRate": 0.0,
    "suspectedIssue": "CORRELATION_MISMATCH"
  },

  "bridgeHint": {
    "possibleStage": "RECEIVER_QUEUE",
    "xmitqHealthy": true
  }
}
```

---

## 🎯 核心能力

* oldest message age
* selector命中率推断
* correlation pattern分析
* bridge状态提示

---

# 🧩 3. 消息全链路追踪（Trace System）

---

## 🎯 目标

解决：

> “消息到底死在哪里？”

---

## 📌 Trace模型

每条消息统一ID：

```text id="t1"
businessId（推荐替代 JMSMessageID）
```

---

## 📊 生命周期状态

```text id="t2"
SEND
→ MQ_A
→ BRIDGE
→ MQ_B
→ CONSUMED
→ COMPLETED
```

---

## 📌 埋点字段

```json id="t3"
{
  "businessId": "xxx",
  "messageId": "xxx",
  "correlationId": "xxx",

  "stage": "BRIDGE",
  "queueManager": "AVQ / WAVQ",
  "queue": "IN_QUEUE",

  "selectorMatched": true,
  "processingTimeMs": 120
}
```

---

## 📈 输出

* 消息流向图
* 卡点统计
* 延迟分析

---

# 🧩 4. MQ Bridge 专项监控（关键模块）

---

## 🎯 目标

解决：

> “消息卡在 bridge 还是消费侧？”

---

## 📊 监控指标

### Channel

* STATUS (RUNNING / RETRY)
* messages in flight
* retry count

---

### XMITQ

* depth
* oldest message age
* throughput

---

### Delivery lag

```text id="b1"
AVQ → WAVQ 延迟
```

---

## 📌 核心告警规则

### ❗ Bridge卡死

```text
XMITQ depth > threshold AND channel not RUNNING
```

---

### ❗ 传输延迟

```text
oldest message age > 300s
```

---

# 五、关键问题解决机制（针对你当前问题）

---

## ❗ 1. selector不可观测问题

### 解决：

增加：

```text id="s1"
selector match rate
```

---

## ❗ 2. JMSMessageID跨MQ失效

### 解决：

改为：

```text id="s2"
businessId（全局唯一）
```

---

## ❗ 3. bridge误判问题

### 解决：

增加：

* XMITQ监控
* channel trace

---

## ❗ 4. “171条假堆积”

自动识别：

```text id="s3"
IN_QUEUE backlog + selector mismatch
```

---

# 六、落地实施方案

---

# 🧱 Phase 1（1~2周）

* MQ Exporter上线
* 基础 dashboard
* queue + channel监控

---

# 🧱 Phase 2（2~4周）

* MQ Diagnose API
* oldest message age
* sample message API

---

# 🧱 Phase 3（4~6周）

* 消息 trace system
* Grafana trace dashboard
* selector命中率

---

# 🧱 Phase 4（优化）

* 自动 root cause detection
* bridge异常预测
* 消费失败自动定位

---

# 七、核心价值总结

---

## ✔ 解决的问题

* ❌ 看不到消息卡在哪里
* ❌ selector导致“假消费”
* ❌ bridge卡死不可见
* ❌ backlog无法解释

---

## ✔ 最终能力

> 从“看 MQ 指标”升级为“看消息生命周期”

---

# 八、一句话总结（银行评审用）

> 本方案通过 MQ Exporter + 诊断API + 全链路Trace + Bridge监控四层体系，实现 IBM MQ 在 AVQ → WAVQ bridge 架构下的消息全生命周期可观测性，解决 selector、bridge 与消费链路不可见导致的生产问题定位困难。

---
