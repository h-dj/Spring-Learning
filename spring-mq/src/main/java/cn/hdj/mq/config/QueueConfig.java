package cn.hdj.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/3/5 下午8:21
 */
@Configuration
public class QueueConfig {


    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public ConfirmCallbackService confirmCallbackService(){
        return new ConfirmCallbackService();
    }
    @Bean
    public ReturnCallbackService returnCallbackService(){
        return new ReturnCallbackService();
    }

    @Bean
    public RabbitTemplateWrapper rabbitTemplateWrapper() {

        //消息投递到 交换机 确认回调
        this.rabbitTemplate.setConfirmCallback(confirmCallbackService());

        //消息投递到  队列 确认回调
        this.rabbitTemplate.setReturnsCallback(returnCallbackService());

        return new RabbitTemplateWrapper(this.rabbitTemplate);
    }

    /**
     * 确认队列
     * @return
     */
    @Bean(name = "confirmTestQueue")
    public Queue confirmTestQueue() {
        return new Queue("confirm_test_queue", true, false, false);
    }

    /**
     * 交换机
     * @return
     */
    @Bean(name = "confirmTestExchange")
    public FanoutExchange confirmTestExchange() {
        return new FanoutExchange("confirmTestExchange");
    }

    /**
     * 测试绑定队列和广播交换机
     * @param confirmTestExchange
     * @param confirmTestQueue
     * @return
     */
    @Bean
    public Binding confirmTestFanoutExchangeAndQueue(
            @Qualifier("confirmTestExchange") FanoutExchange confirmTestExchange,
            @Qualifier("confirmTestQueue") Queue confirmTestQueue) {
        return BindingBuilder.bind(confirmTestQueue).to(confirmTestExchange);
    }
}

