package cn.reid.springjmsibm.service;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用 IBM MQ 原生 API（MQQueueManager）发送消息的服务。
 * <p>
 * 与 {@link MqMessageService}（基于 JmsTemplate）不同，此服务直接操作 MQMD 字段，
 * 支持自定义 {@code MQMD.MessageId}，实现业务自定义 JMSMessageID 的需求。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqNativeMessageService {

    private final MQQueueManager mqQueueManager;

    @Value("${ibm.mq.send-queue:DEV.QUEUE.2}")
    private String defaultSendQueue;

    /**
     * 使用 IBM MQ 原生 API 发送文本消息，支持自定义 MQMD.MessageId。
     *
     * @param queueName    目标队列名，为空时使用配置的默认 send-queue
     * @param messageBody  消息正文（文本）
     * @param messageIdHex 自定义 MessageId，hex 字符串（如 "AABBCCDD"），
     *                     转 24 字节写入 MQMD，不足右侧补零，超出截断；
     *                     为空时由 MQ 自动生成
     * @return 结果 Map，包含 queue、messageId（JMS 格式 "ID:" + 48位hex）、messageLength
     * @throws MQException               MQ 操作失败
     * @throws IllegalArgumentException  hex 格式无效
     */
    public Map<String, Object> sendTextMessage(String queueName, String messageBody, String messageIdHex)
            throws MQException {
        try {
            return doSendTextMessage(queueName, messageBody, messageIdHex);
        } catch (IOException e) {
            log.error("Failed to write message body", e);
            throw new RuntimeException("Failed to write message body: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> doSendTextMessage(String queueName, String messageBody, String messageIdHex)
            throws MQException, IOException {

        String targetQueue = (queueName != null && !queueName.isBlank()) ? queueName : defaultSendQueue;

        MQMessage mqMsg = new MQMessage();

        // 设置消息格式和字符集
        mqMsg.format = CMQC.MQFMT_STRING;
        mqMsg.characterSet = 1208; // UTF-8 CCSID

        // 自定义 MQMD.MessageId
        boolean hasCustomMsgId = (messageIdHex != null && !messageIdHex.isBlank());
        if (hasCustomMsgId) {
            mqMsg.messageId = toMqmdBytes24(messageIdHex);
        }

        // 写入消息体
        mqMsg.writeString(messageBody);

        // Put 选项
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        // 默认情况下 MQ 会生成新的 MessageId（MQPMO_NEW_MSG_ID），
        // 当提供了自定义 ID 时不加该选项，MQ 将保留 MQMD 中的值
        if (!hasCustomMsgId) {
            pmo.options = CMQC.MQPMO_NEW_MSG_ID | CMQC.MQPMO_FAIL_IF_QUIESCING;
        } else {
            pmo.options = CMQC.MQPMO_FAIL_IF_QUIESCING;
        }

        // 打开队列并发送
        MQQueue queue = mqQueueManager.accessQueue(targetQueue, CMQC.MQOO_OUTPUT, null, null, null);
        try {
            queue.put(mqMsg, pmo);

            // JMS 规范: JMSMessageID = "ID:" + 48位 hex
            String jmsMessageId = "ID:" + bytesToHex(mqMsg.messageId);
            log.info("Sent message to queue [{}] with messageId=[{}] body=[{}]",
                    targetQueue, jmsMessageId, messageBody);

            Map<String, Object> result = new HashMap<>();
            result.put("queue", targetQueue);
            result.put("messageId", jmsMessageId);
            result.put("messageLength", messageBody.getBytes(StandardCharsets.UTF_8).length);
            return result;
        } finally {
            closeSafely(queue);
        }
    }

    // ── 工具方法 ──

    /**
     * 将 hex 字符串转换为 24 字节数组（MQMD.MessageId / CorrelationId 固定长度）。
     * 不足 24 字节时右侧补零；超出时截断。
     */
    private byte[] toMqmdBytes24(String hex) {
        byte[] raw = hexStringToBytes(hex);
        byte[] padded = new byte[24];
        int copyLen = Math.min(raw.length, 24);
        System.arraycopy(raw, 0, padded, 0, copyLen);
        return padded;
    }

    /**
     * 解析 hex 字符串为 byte[]。
     * 支持可选 "0x"/"0X" 前缀，自动去除空格和分隔符。
     *
     * @throws IllegalArgumentException 若字符串含非 hex 字符或长度为奇数
     */
    private byte[] hexStringToBytes(String hex) {
        String cleaned = hex;
        if (cleaned.startsWith("0x") || cleaned.startsWith("0X")) {
            cleaned = cleaned.substring(2);
        }
        cleaned = cleaned.replaceAll("[^0-9A-Fa-f]", "");
        if (cleaned.isEmpty() || cleaned.length() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Invalid hex string '" + hex + "': must contain an even number of hex characters");
        }
        int len = cleaned.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(cleaned.charAt(i), 16) << 4)
                    | Character.digit(cleaned.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 将 byte[] 转为大写 hex 字符串。
     */
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

    private void closeSafely(MQQueue queue) {
        try {
            queue.close();
        } catch (MQException e) {
            log.warn("Failed to close queue [{}]: {}", queue.name, e.getMessage());
        }
    }
}
