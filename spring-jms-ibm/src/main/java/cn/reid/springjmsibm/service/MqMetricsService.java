package cn.reid.springjmsibm.service;

import cn.reid.springjmsibm.dto.QueueMetricsDTO;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqMetricsService {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_BROWSE_FOR_NEWEST = 500;

    private final MQQueueManager mqQueueManager;

    /**
     * 批量查询多个队列的监控指标，单个失败不影响其他队列
     */
    public List<QueueMetricsDTO> getQueueMetrics(List<String> queueNames) {
        List<QueueMetricsDTO> results = new ArrayList<>(queueNames.size());
        for (String queueName : queueNames) {
            try {
                results.add(getSingleQueueMetrics(queueName));
            } catch (Exception e) {
                log.warn("Failed to get metrics for queue [{}]: {}", queueName, e.getMessage());
                results.add(QueueMetricsDTO.builder()
                        .queueName(queueName)
                        .error(e.getMessage())
                        .build());
            }
        }
        return results;
    }

    /**
     * 查询单个队列的监控指标
     * <p>
     * 分两阶段:
     * 1. MQOO_INQUIRE 打开 → 用 getter 方法获取主要指标 + inquire 获取无 getter 的指标
     * 2. 若有消息 → MQOO_BROWSE 打开浏览最老/最新消息时间
     */
    public QueueMetricsDTO getSingleQueueMetrics(String queueName) throws MQException {
        // Phase 1: 获取队列属性
        MQQueue queue = mqQueueManager.accessQueue(queueName, CMQC.MQOO_INQUIRE,
                null, null, null);
        int currentDepth, maxDepth, openInputCount, openOutputCount, queueType,
                inhibitGetVal, inhibitPutVal;
        int backoutThreshold = 0;
        long totalEnqueueCount = 0;
        long totalDequeueCount = 0;
        try {
            currentDepth = queue.getCurrentDepth();
            maxDepth = queue.getMaximumDepth();
            openInputCount = queue.getOpenInputCount();
            openOutputCount = queue.getOpenOutputCount();
            queueType = queue.getQueueType();
            inhibitGetVal = queue.getInhibitGet();
            inhibitPutVal = queue.getInhibitPut();

            // 以下属性可能因 MQ 版本或权限不支持，逐个尝试
            backoutThreshold = inquireIntSafely(queue, CMQC.MQIA_BACKOUT_THRESHOLD);
            totalEnqueueCount = inquireIntSafely(queue, CMQC.MQIA_MSG_ENQ_COUNT);
            totalDequeueCount = inquireIntSafely(queue, CMQC.MQIA_MSG_DEQ_COUNT);
        } finally {
            closeSafely(queue);
        }

        // Phase 2: 若有消息则浏览时间信息
        String oldestMessagePutTime = null;
        String newestMessagePutTime = null;
        long oldestMessageAge = -1;

        if (currentDepth > 0) {
            MQQueue browseQueue = mqQueueManager.accessQueue(queueName, CMQC.MQOO_BROWSE,
                    null, null, null);
            try {
                MessageTimeResult timeResult = browseMessageTimes(browseQueue, currentDepth);
                oldestMessagePutTime = timeResult.oldestPutTime;
                oldestMessageAge = timeResult.oldestAgeSeconds;
                newestMessagePutTime = timeResult.newestPutTime;
            } finally {
                closeSafely(browseQueue);
            }
        }

        return QueueMetricsDTO.builder()
                .queueName(queueName)
                .currentDepth(currentDepth)
                .maxDepth(maxDepth)
                .openInputCount(openInputCount)
                .openOutputCount(openOutputCount)
                .queueType(mapQueueType(queueType))
                .inhibitGet(inhibitGetVal == CMQC.MQQA_GET_INHIBITED ? "DISABLED" : "ENABLED")
                .inhibitPut(inhibitPutVal == CMQC.MQQA_PUT_INHIBITED ? "DISABLED" : "ENABLED")
                .backoutThreshold(backoutThreshold)
                .totalEnqueueCount(totalEnqueueCount)
                .totalDequeueCount(totalDequeueCount)
                .oldestMessageAge(oldestMessageAge)
                .oldestMessagePutTime(oldestMessagePutTime)
                .newestMessagePutTime(newestMessagePutTime)
                .build();
    }

    /**
     * 安全地 inquire 单个整数属性，失败时返回 0 并记录警告
     */
    private int inquireIntSafely(MQQueue queue, int selector) {
        try {
            int[] selectors = {selector};
            int[] result = new int[1];
            queue.inquire(selectors, result, (byte[]) null);
            return result[0];
        } catch (MQException e) {
            log.warn("Failed to inquire attribute {} on queue [{}]: {}",
                    attributeName(selector), queue.name, e.getMessage());
            return 0;
        }
    }

    /**
     * 从队列头部开始浏览消息，获取最老和最新消息的 put 时间
     */
    private MessageTimeResult browseMessageTimes(MQQueue queue, int queueDepth) {
        int toBrowse = Math.min(queueDepth, MAX_BROWSE_FOR_NEWEST);
        int browsed = 0;
        Calendar oldestPut = null;
        Calendar newestPut = null;

        try {
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = CMQC.MQGMO_BROWSE_FIRST
                    | CMQC.MQGMO_FAIL_IF_QUIESCING
                    | CMQC.MQGMO_NO_WAIT;
            gmo.matchOptions = CMQC.MQMO_NONE;

            while (browsed < toBrowse) {
                MQMessage mqMsg = new MQMessage();
                queue.get(mqMsg, gmo);

                if (oldestPut == null) {
                    oldestPut = mqMsg.putDateTime;
                }
                newestPut = mqMsg.putDateTime;
                browsed++;

                gmo.options = CMQC.MQGMO_BROWSE_NEXT
                        | CMQC.MQGMO_FAIL_IF_QUIESCING
                        | CMQC.MQGMO_NO_WAIT;
            }
        } catch (MQException e) {
            if (e.reasonCode != CMQC.MQRC_NO_MSG_AVAILABLE) {
                log.warn("Error browsing messages for time range: {}", e.getMessage());
            }
        }

        MessageTimeResult result = new MessageTimeResult();
        if (oldestPut != null) {
            result.oldestPutTime = formatCalendar(oldestPut);
            result.oldestAgeSeconds = (System.currentTimeMillis() - oldestPut.getTimeInMillis()) / 1000;
        }
        if (newestPut != null) {
            result.newestPutTime = formatCalendar(newestPut);
        }
        if (browsed < queueDepth) {
            log.debug("Reached browse limit {} for queue with depth {}, newest put time may be inaccurate",
                    MAX_BROWSE_FOR_NEWEST, queueDepth);
        }
        return result;
    }

    private static void closeSafely(MQQueue queue) {
        try {
            queue.close();
        } catch (MQException e) {
            log.warn("Failed to close queue [{}]: {}", queue.name, e.getMessage());
        }
    }

    private static String formatCalendar(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(calendar.toInstant(), ZoneOffset.UTC);
        return ldt.format(DATE_TIME_FMT);
    }

    private static String mapQueueType(int type) {
        switch (type) {
            case CMQC.MQQT_LOCAL:
                return "LOCAL";
            case CMQC.MQQT_ALIAS:
                return "ALIAS";
            case CMQC.MQQT_REMOTE:
                return "REMOTE";
            case CMQC.MQQT_MODEL:
                return "MODEL";
            default:
                return "UNKNOWN(" + type + ")";
        }
    }

    private static String attributeName(int selector) {
        if (selector == CMQC.MQIA_BACKOUT_THRESHOLD) return "BACKOUT_THRESHOLD";
        if (selector == CMQC.MQIA_MSG_ENQ_COUNT) return "MSG_ENQ_COUNT";
        if (selector == CMQC.MQIA_MSG_DEQ_COUNT) return "MSG_DEQ_COUNT";
        return String.valueOf(selector);
    }

    private static class MessageTimeResult {
        String oldestPutTime;
        long oldestAgeSeconds;
        String newestPutTime;
    }
}
