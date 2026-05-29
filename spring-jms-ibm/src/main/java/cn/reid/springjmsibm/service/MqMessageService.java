package cn.reid.springjmsibm.service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqMessageService {

    private final JmsTemplate jmsTemplate;

    @Value("${ibm.mq.send-queue:DEV.QUEUE.2}")
    private String sendQueue;

    /**
     * 发送文本消息到指定队列
     */
    public void sendTextMessage(String message) {
        jmsTemplate.send(sendQueue, session -> {
            TextMessage textMessage = session.createTextMessage(message);
            log.info("Sending message to queue [{}]: {}", sendQueue, message);
            return textMessage;
        });
    }

    /**
     * 发送文本消息到指定队列
     */
    public void sendTextMessage(String queueName, String message) {
        jmsTemplate.send(queueName, session -> {
            TextMessage textMessage = session.createTextMessage(message);
            log.info("Sending message to queue [{}]: {}", queueName, message);
            return textMessage;
        });
    }

    /**
     * 同步接收消息（等待指定时间）
     */
    public String receiveMessage(long timeoutMillis) {
        long originalTimeout = jmsTemplate.getReceiveTimeout();
        jmsTemplate.setReceiveTimeout(timeoutMillis);
        try {
            Message message = jmsTemplate.receive(sendQueue);
            if (message instanceof TextMessage textMessage) {
                try {
                    String text = textMessage.getText();
                    log.info("Received message from queue [{}]: {}", sendQueue, text);
                    return text;
                } catch (JMSException e) {
                    log.error("Failed to read message", e);
                    return null;
                }
            }
            return null;
        } finally {
            jmsTemplate.setReceiveTimeout(originalTimeout);
        }
    }
}
