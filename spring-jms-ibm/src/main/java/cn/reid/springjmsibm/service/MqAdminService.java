package cn.reid.springjmsibm.service;

import cn.reid.springjmsibm.dto.BrowseFilter;
import cn.reid.springjmsibm.dto.BrowseMessageDTO;
import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqAdminService {

    private static final int MAX_BODY_BYTES = 100 * 1024; // 100KB
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MQQueueManager mqQueueManager;

    /**
     * 获取指定队列的深度（当前消息数）
     */
    public int getQueueDepth(String queueName) {
        try {
            int openOptions = CMQC.MQOO_INQUIRE;
            MQQueue queue = mqQueueManager.accessQueue(queueName, openOptions, null, null, null);
            int depth = queue.getCurrentDepth();
            queue.close();
            log.info("Queue [{}] depth: {}", queueName, depth);
            return depth;
        } catch (MQException e) {
            log.error("Failed to get queue depth for [{}]", queueName, e);
            throw new RuntimeException("Failed to get queue depth: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * 使用 IBM MQ 原生 API 浏览队列消息（非破坏性读取）
     */
    public List<BrowseMessageDTO> browseMessages(String queueName, BrowseFilter filter) {
        List<BrowseMessageDTO> results = new ArrayList<>();
        try {
            int openOptions = CMQC.MQOO_BROWSE;
            MQQueue queue = mqQueueManager.accessQueue(queueName, openOptions, null, null, null);

            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = CMQC.MQGMO_BROWSE_FIRST | CMQC.MQGMO_FAIL_IF_QUIESCING | CMQC.MQGMO_NO_WAIT;
            gmo.matchOptions = CMQC.MQMO_NONE;

            int position = 0;
            int collected = 0;
            int skipped = 0;

            while (true) {
                MQMessage mqMsg = new MQMessage();
                try {
                    queue.get(mqMsg, gmo);
                    position++;

                    // Switch to BROWSE_NEXT for subsequent messages
                    gmo.options = CMQC.MQGMO_BROWSE_NEXT | CMQC.MQGMO_FAIL_IF_QUIESCING | CMQC.MQGMO_NO_WAIT;

                    // Build DTO
                    int msgSize;
                    try {
                        msgSize = mqMsg.getMessageLength();
                    } catch (IOException e) {
                        log.warn("Failed to get message length at position {}", position);
                        msgSize = 0;
                    }
                    BrowseMessageDTO.BrowseMessageDTOBuilder builder = BrowseMessageDTO.builder()
                            .position(position)
                            .messageId(bytesToHex(mqMsg.messageId))
                            .correlId(bytesToHex(mqMsg.correlationId))
                            .replyToQueueName(mqMsg.replyToQueueName)
                            .replyToQueueManager(mqMsg.replyToQueueManagerName)
                            .putUserId(mqMsg.userId)
                            .putApplName(mqMsg.putApplicationName)
                            .backoutCount(mqMsg.backoutCount)
                            .priority(mqMsg.priority)
                            .messageSize(msgSize);


                    // Format timestamp from MQMD PutDate/PutTime
                    builder.timestamp(formatPutDateTime(mqMsg.putDateTime));

                    // Skip if timestamp filter doesn't match
                    if (!passesTimeFilter(builder.build(), filter)) {
                        skipped++;
                        continue;
                    }

                    // Skip if msgId filter doesn't match
                    if (!passesMsgIdFilter(bytesToHex(mqMsg.messageId), filter)) {
                        skipped++;
                        continue;
                    }

                    // Read message body
                    String bodyBase64 = readBodyAsBase64(mqMsg, msgSize);
                    builder.bodyBase64(bodyBase64);

                    BrowseMessageDTO dto = builder.build();

                    // Apply offset/skip
                    if (skipped + collected < filter.getOffset()) {
                        skipped++;
                        continue;
                    }

                    results.add(dto);
                    collected++;

                    if (collected >= filter.getLimit()) {
                        break;
                    }

                } catch (MQException e) {
                    if (e.reasonCode == CMQC.MQRC_NO_MSG_AVAILABLE) {
                        break;
                    }
                    log.warn("Error browsing message at position {}: {}", position, e.getLocalizedMessage());
                    // Try to continue with next message
                    gmo.options = CMQC.MQGMO_BROWSE_NEXT | CMQC.MQGMO_FAIL_IF_QUIESCING | CMQC.MQGMO_NO_WAIT;
                }
            }

            queue.close();
            log.info("Browsed queue [{}]: total={}, collected={}, filtered/skipped={}",
                    queueName, position, collected, skipped);

        } catch (MQException e) {
            log.error("Failed to browse queue [{}]", queueName, e);
            throw new RuntimeException("Failed to browse queue: " + e.getLocalizedMessage(), e);
        }

        return results;
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    private String formatPutDateTime(Calendar calendar) {
        if (calendar == null) {
            return "";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(calendar.toInstant(), ZoneOffset.UTC);
        return ldt.format(DATE_TIME_FMT);
    }

    private boolean passesMsgIdFilter(String msgIdHex, BrowseFilter filter) {
        if (filter.getMsgId() == null || filter.getMsgId().isEmpty()) {
            return true;
        }
        return msgIdHex.startsWith(filter.getMsgId().toUpperCase());
    }

    private boolean passesTimeFilter(BrowseMessageDTO dto, BrowseFilter filter) {
        if (dto.getTimestamp() == null || dto.getTimestamp().isEmpty()) {
            return true;
        }
        try {
            LocalDateTime msgTime = LocalDateTime.parse(dto.getTimestamp(), DATE_TIME_FMT);
            if (filter.getStartTime() != null && !filter.getStartTime().isEmpty()) {
                LocalDateTime start = LocalDateTime.parse(filter.getStartTime(), DATE_TIME_FMT);
                if (msgTime.isBefore(start)) {
                    return false;
                }
            }
            if (filter.getEndTime() != null && !filter.getEndTime().isEmpty()) {
                LocalDateTime end = LocalDateTime.parse(filter.getEndTime(), DATE_TIME_FMT);
                if (msgTime.isAfter(end)) {
                    return false;
                }
            }
        } catch (DateTimeParseException e) {
            log.warn("Invalid time format in filter, skip time filter: {}", e.getMessage());
        }
        return true;
    }

    private String readBodyAsBase64(MQMessage mqMsg, int msgSize) {
        try {
            int readSize = Math.min(msgSize, MAX_BODY_BYTES);
            byte[] body = new byte[readSize];
            mqMsg.readFully(body);
            return Base64.getEncoder().encodeToString(body);
        } catch (Exception e) {
            log.warn("Failed to read message body: {}", e.getMessage());
            return "";
        }
    }
}
