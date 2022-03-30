package cn.hdj.mq.config;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/3/5 下午10:07
 */
public class RabbitTemplateWrapper {

    private RabbitTemplate rabbitTemplate;

    public RabbitTemplateWrapper(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void convertAndSend(String exchange, String routingKey, Object msg){
        this.convertAndSend(exchange,routingKey,msg,null);
    }


    public void convertAndSend(String exchange, String routingKey, Object msg,String expiration){
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties messageProperties = message.getMessageProperties();
                if(exchange!=null){
                    messageProperties.setExpiration(expiration);
                }
                messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                messageProperties.setCorrelationId(UUID.randomUUID().toString());
                return message;
            }
        });
    }
}
