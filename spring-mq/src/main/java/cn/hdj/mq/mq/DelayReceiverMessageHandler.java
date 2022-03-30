package cn.hdj.mq.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Description: 延迟消息监听器
 * @Author huangjiajian
 * @Date 2022/3/5 下午10:15
 */
@Slf4j
@Component
//监听队列
@RabbitListener(queues = "lock_merchant_dead_queue")
public class DelayReceiverMessageHandler {

    @RabbitHandler
    public void processHandler(String msg, Channel channel, Message message) throws IOException {

        try {
            log.info("小富收到消息：{}", msg);

            //TODO 具体业务
            log.info("队列延迟的时间 {} ",message.getMessageProperties().getDelay());
            log.info("接受时间 {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            //手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        }  catch (Exception e) {

            if (message.getMessageProperties().getRedelivered()) {

                log.error("消息已重复处理失败,拒绝再次接收...");

                // 拒绝消息
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {

                log.error("消息即将再次返回队列处理...");
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }
}