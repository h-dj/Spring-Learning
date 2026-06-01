package cn.reid.springjmsibm.listener;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqMessageListener {

    @Value("${ibm.mq.receive-queue:DEV.QUEUE.1}")
    private String receiveQueue;

    /**
     * 监听 IBM MQ 队列，自动接收消息
     * 使用默认的 JmsListenerContainerFactory（由 mq-jms-spring-boot-starter 自动配置）
     */
//    @JmsListener(destination = "DEV.QUEUE.1")
    public void onMessage(Message message) {
        if (message instanceof TextMessage textMessage) {
            try {
                String text = textMessage.getText();
                String messageId = textMessage.getJMSMessageID();
                log.info("Received message [{}] from queue [{}]: {}", messageId, receiveQueue, text);
                // 业务处理...
            } catch (JMSException e) {
                log.error("Error processing message from queue [{}]", receiveQueue, e);
            }
        } else {
            log.warn("Received non-text message from queue [{}]: {}", receiveQueue, message);
        }
    }
}
