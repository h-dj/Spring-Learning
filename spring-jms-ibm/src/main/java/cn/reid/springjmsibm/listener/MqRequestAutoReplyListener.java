package cn.reid.springjmsibm.listener;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * 请求自动回复监听器（模拟 B 系统）。
 * <p>
 * 监听 DEV.QUEUE.2（B-in 队列），收到请求后自动回复到
 * 请求消息指定的 JMSReplyTo 队列，并将请求的 JMSMessageID 设为回复的 JMSCorrelationID。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqRequestAutoReplyListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "DEV.QUEUE.2")
    public void onRequest(Message message) {
        try {
            if (!(message instanceof TextMessage textMessage)) {
                log.warn("Received non-text request, skipping auto-reply: {}", message);
                return;
            }

            String requestText = textMessage.getText();
            String requestMsgId = textMessage.getJMSMessageID();
            String correlId = textMessage.getJMSCorrelationID();
            Destination replyTo = textMessage.getJMSReplyTo();

            log.info("Received request [msgId={}, correlId={}] on DEV.QUEUE.2: {}",
                    requestMsgId, correlId, requestText);

            if (replyTo == null) {
                log.warn("Request has no JMSReplyTo, cannot auto-reply. msgId={}", requestMsgId);
                return;
            }

            String replyQueueName = (replyTo instanceof Queue q) ? q.getQueueName() : replyTo.toString();
            log.info("Auto-replying to [{}] with JMSCorrelationID=[{}]", replyQueueName, requestMsgId);

            // 发送回复，JMSCorrelationID 设为请求消息的 JMSMessageID
            jmsTemplate.send(replyTo, session -> {
                TextMessage reply = session.createTextMessage("Reply to: " + requestText);
                reply.setJMSCorrelationID(requestMsgId);
                return reply;
            });

        } catch (JMSException e) {
            log.error("Error processing request auto-reply", e);
        }
    }
}
