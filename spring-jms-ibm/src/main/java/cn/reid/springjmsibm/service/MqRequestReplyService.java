package cn.reid.springjmsibm.service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 请求-回复模式服务。
 * <p>
 * A 将请求发送到 A-out 队列（DEV.QUEUE.2），
 * B 从 B-in 队列（DEV.QUEUE.2）消费并自动回复，
 * A 在 A-in 队列（DEV.QUEUE.1）通过 JMSCorrelationID 匹配回复。
 * <p>
 * 使用 {@link #replyToQueueManager} 指定回复目标队列管理器，
 * 避免跨 QMgr 场景下回复消息路由失败。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqRequestReplyService {

    private final JmsTemplate jmsTemplate;

    @Value("${ibm.mq.send-queue:DEV.QUEUE.2}")
    private String requestQueue;

    @Value("${ibm.mq.receive-queue:DEV.QUEUE.1}")
    private String replyQueue;

    @Value("${ibm.mq.queue-manager:QM1}")
    private String replyToQueueManager;

    /**
     * 发送请求消息并同步等待回复（通过 JMSCorrelationID 匹配）
     *
     * @param requestText  请求消息体
     * @param timeoutMillis 等待回复的超时时间（毫秒）
     * @return 回复消息的文本内容，超时返回 null
     */
    public String sendAndReceive(String requestText, long timeoutMillis) {
        String correlId = UUID.randomUUID().toString();
        String selector = "JMSCorrelationID = 'ID:" + correlId + "'";

        // 发送请求消息，设置 JMSCorrelationID、JMSReplyTo 和 replyToQueueManager
        jmsTemplate.send(requestQueue, session -> {
            TextMessage msg = session.createTextMessage(requestText);
            msg.setJMSCorrelationID("ID:" + correlId);
            msg.setJMSReplyTo(session.createQueue(replyQueue));
            msg.setStringProperty("JMS_IBM_ReplyToQMgr", replyToQueueManager);
            log.info("Sending request [correlId={}] to queue [{}], replyTo=[{}], replyToQMgr=[{}]",
                    correlId, requestQueue, replyQueue, replyToQueueManager);
            return msg;
        });

        // 同步等待匹配的回复消息
        log.info("Waiting for reply on queue [{}] with selector: {}", replyQueue, selector);

        long originalTimeout = jmsTemplate.getReceiveTimeout();
        jmsTemplate.setReceiveTimeout(timeoutMillis);
        try {
            Message reply = jmsTemplate.receiveSelected(replyQueue, selector);
            if (reply instanceof TextMessage textReply) {
                String text = textReply.getText();
                log.info("Received reply [correlId={}] from queue [{}]: {}", correlId, replyQueue, text);
                return text;
            }
            if (reply != null) {
                log.warn("Received non-text reply [correlId={}]: {}", correlId, reply);
            }
            log.warn("No reply received within {}ms for correlId [{}]", timeoutMillis, correlId);
            return null;
        } catch (JMSException e) {
            log.error("Failed to process reply for correlId [{}]", correlId, e);
            return null;
        } finally {
            jmsTemplate.setReceiveTimeout(originalTimeout);
        }
    }
}
