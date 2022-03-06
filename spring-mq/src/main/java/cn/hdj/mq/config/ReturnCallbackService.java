package cn.hdj.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/3/5 下午8:55
 */
@Slf4j
public class ReturnCallbackService implements RabbitTemplate.ReturnsCallback {

    /**
     * 如果消息未能投递到目标 queue 里将触发回调 returnCallback ，
     * 一旦向 queue 投递消息未成功，
     * 这里一般会记录下当前消息的详细投递数据，方便后续做重发或者补偿等操作。
     * @param returnedMessage
     */
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.info("returnedMessage ===> replyCode={} ,replyText={} ,exchange={} ,routingKey={}"
                , returnedMessage.getReplyCode()
                , returnedMessage.getReplyText()
                , returnedMessage.getMessage()
                , returnedMessage.getRoutingKey());

    }
}
