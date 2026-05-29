package cn.reid.springjmsibm.service;

import cn.reid.springjmsibm.dto.BrowseFilter;
import cn.reid.springjmsibm.dto.BrowseMessageDTO;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JmsBrowseService {

    private static final int MAX_BODY_BYTES = 100 * 1024; // 100KB
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JmsTemplate jmsTemplate;

    /**
     * 使用 JMS QueueBrowser 浏览队列消息（非破坏性读取），
     * 通过 messageSelector 将过滤条件下推到 MQ broker 端，减少网络传输。
     */
    public List<BrowseMessageDTO> browseMessages(String queueName, BrowseFilter filter) {
        return jmsTemplate.execute((Session session) -> {
            List<BrowseMessageDTO> results = new ArrayList<>();

            String selector = buildMessageSelector(filter);
            log.debug("JMS browse selector: [{}]", selector);

            QueueBrowser browser = (selector != null)
                    ? session.createBrowser(session.createQueue(queueName), selector)
                    : session.createBrowser(session.createQueue(queueName));
            try {
                Enumeration<?> enumeration = browser.getEnumeration();

                int position = 0;
                int collected = 0;
                int skipped = 0;

                while (enumeration.hasMoreElements()) {
                    Message msg = (Message) enumeration.nextElement();
                    position++;

                    try {
                        // JMSTimestamp 已经在 broker 端过滤了，但再次确认以免时区问题
                        long timestamp = msg.getJMSTimestamp();
                        int msgSize = getMessageSize(msg);

                        // 时间再校验一次（broker 端用毫秒过滤，确保 range 精确）
                        if (!passesTimeFilter(timestamp, filter)) {
                            skipped++;
                            continue;
                        }

                        String msgId = stripMsgIdPrefix(msg.getJMSMessageID());
                        String correlId = stripMsgIdPrefix(msg.getJMSCorrelationID());
                        String replyToQueueName = getReplyToQueueName(msg);
                        String replyToQueueManager = getStringProperty(msg, "JMS_IBM_ReplyToQMgr");
                        String putUserId = getStringProperty(msg, "JMSXUserID");
                        String putApplName = getStringProperty(msg, "JMSXAppID");

                        // Apply offset
                        if (skipped + collected < filter.getOffset()) {
                            skipped++;
                            continue;
                        }

                        String bodyBase64 = readBodyAsBase64(msg);

                        BrowseMessageDTO dto = BrowseMessageDTO.builder()
                                .position(position)
                                .messageId(msgId)
                                .correlId(correlId)
                                .replyToQueueName(replyToQueueName)
                                .replyToQueueManager(replyToQueueManager)
                                .putUserId(putUserId)
                                .putApplName(putApplName)
                                .timestamp(formatTimestamp(timestamp))
                                .messageSize(msgSize)
                                .bodyBase64(bodyBase64)
                                .build();

                        results.add(dto);
                        collected++;

                        if (collected >= filter.getLimit()) {
                            break;
                        }

                    } catch (JMSException e) {
                        log.warn("Error reading JMS message at position {}: {}", position, e.getMessage());
                    }
                }

                log.info("JMS browsed queue [{}]: total={}, collected={}, filtered/skipped={}",
                        queueName, position, collected, skipped);
            } finally {
                browser.close();
            }

            return results;
        });
    }

    /**
     * 根据 BrowseFilter 构建 JMS messageSelector 字符串，
     * 将支持的下推条件发送到 MQ broker 端过滤。
     */
    private String buildMessageSelector(BrowseFilter filter) {
        List<String> clauses = new ArrayList<>();

        // JMSMessageID 前缀匹配（用户传入的不带 "ID:" 前缀）
        if (filter.getMsgId() != null && !filter.getMsgId().isEmpty()) {
            clauses.add("JMSMessageID LIKE 'ID:" + escapeSelectorString(filter.getMsgId()) + "%'");
        }

        // JMSXUserID 模糊匹配
        if (filter.getPutUserId() != null && !filter.getPutUserId().isEmpty()) {
            clauses.add("JMSXUserID LIKE '%" + escapeSelectorString(filter.getPutUserId()) + "%'");
        }

        // JMSXAppID 模糊匹配
        if (filter.getPutApplName() != null && !filter.getPutApplName().isEmpty()) {
            clauses.add("JMSXAppID LIKE '%" + escapeSelectorString(filter.getPutApplName()) + "%'");
        }

        // JMSTimestamp 范围（将日期字符串转为 epoch 毫秒）
        if (filter.getStartTime() != null && !filter.getStartTime().isEmpty()) {
            Long epochMillis = parseTimeToEpochMillis(filter.getStartTime());
            if (epochMillis != null) {
                clauses.add("JMSTimestamp >= " + epochMillis);
            }
        }
        if (filter.getEndTime() != null && !filter.getEndTime().isEmpty()) {
            Long epochMillis = parseTimeToEpochMillis(filter.getEndTime());
            if (epochMillis != null) {
                clauses.add("JMSTimestamp <= " + epochMillis);
            }
        }

        if (clauses.isEmpty()) {
            return null;
        }
        return String.join(" AND ", clauses);
    }

    /**
     * 转义 JMS selector 字符串中的单引号（用两个单引号表示一个文字单引号）
     */
    private String escapeSelectorString(String value) {
        return value.replace("'", "''");
    }

    /**
     * 将 yyyy-MM-dd HH:mm:ss 格式的时间字符串转为 epoch 毫秒（UTC）
     */
    private Long parseTimeToEpochMillis(String timeStr) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(timeStr, DATE_TIME_FMT);
            return ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (DateTimeParseException e) {
            log.warn("Invalid time format '{}', expected yyyy-MM-dd HH:mm:ss", timeStr);
            return null;
        }
    }

    private int getMessageSize(Message msg) throws JMSException {
        if (msg instanceof TextMessage textMsg) {
            String text = textMsg.getText();
            return text != null ? text.getBytes(StandardCharsets.UTF_8).length : 0;
        } else if (msg instanceof BytesMessage bytesMsg) {
            return (int) bytesMsg.getBodyLength();
        }
        return 0;
    }

    private String readBodyAsBase64(Message msg) throws JMSException {
        if (msg instanceof TextMessage textMsg) {
            String text = textMsg.getText();
            if (text == null) {
                return "";
            }
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            boolean truncated = bytes.length > MAX_BODY_BYTES;
            byte[] data = truncated ? java.util.Arrays.copyOf(bytes, MAX_BODY_BYTES) : bytes;
            return Base64.getEncoder().encodeToString(data);
        } else if (msg instanceof BytesMessage bytesMsg) {
            long bodyLength = bytesMsg.getBodyLength();
            int readSize = (int) Math.min(bodyLength, MAX_BODY_BYTES);
            byte[] buf = new byte[readSize];
            bytesMsg.readBytes(buf, readSize);
            return Base64.getEncoder().encodeToString(buf);
        }
        return "";
    }

    private String stripMsgIdPrefix(String value) {
        if (value == null) {
            return "";
        }
        if (value.startsWith("ID:")) {
            return value.substring(3);
        }
        return value;
    }

    private String getReplyToQueueName(Message msg) throws JMSException {
        jakarta.jms.Destination dest = msg.getJMSReplyTo();
        if (dest instanceof Queue replyQueue) {
            return replyQueue.getQueueName();
        }
        return null;
    }

    private String getStringProperty(Message msg, String propertyName) {
        try {
            return msg.getStringProperty(propertyName);
        } catch (JMSException e) {
            return null;
        }
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
        return ldt.format(DATE_TIME_FMT);
    }

    private boolean passesTimeFilter(long timestamp, BrowseFilter filter) {
        if (timestamp <= 0) {
            return true;
        }
        try {
            LocalDateTime msgTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
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
}
