package cn.hdj.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/3/5 下午8:52
 */
@Slf4j
public class ConfirmCallbackService implements RabbitTemplate.ConfirmCallback {

    /**
     * correlationData：对象内部只有一个 id 属性，用来表示当前消息的唯一性。
     * ack：消息投递到 broker 的状态，true 表示成功。
     * cause：表示投递失败的原因。
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

        if (!ack) {
            log.error("消息发送异常!");
        } else {
            log.info("发送者已经收到确认，correlationData={} ,ack={}, cause={}", correlationData.getId(), ack, cause);
        }
    }
}