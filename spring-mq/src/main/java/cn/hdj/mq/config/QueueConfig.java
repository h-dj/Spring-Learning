package cn.hdj.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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


    //=======================================

    /**
     * 定义过期队列
     *
     * @return
     */
    @Bean
    public Queue expiresQueue() {
        Map<String, Object> arg = new HashMap<String, Object>();
        arg.put("x-expires", 60000);
        return new Queue("test_delay_queue",true,false,false,arg);
    }





// ========================================
    /**
     * 创建死信交换机
     * @return
     */
    @Bean
    public Exchange lockMerchantDeadExchange(){
        return new TopicExchange("lock_merchant_dead_exchange",true,false);
    }

    /**
     * 创建死信队列
     * @return
     */
    @Bean
    public Queue lockMerchantDeadQueue(){
        return QueueBuilder.durable("lock_merchant_dead_queue").build();
    }

    /**
     * 绑定死信交换机和死信队列
     * @return
     */
    @Bean
    public Binding lockMerchantBinding(){

        return new Binding("lock_merchant_dead_queue"
                ,Binding.DestinationType.QUEUE,
                "lock_merchant_dead_exchange"
                ,"lock_merchant_routing_key"
                ,null);
    }


    /**
     * 创建普通交换机
     * @return
     */
    @Bean
    public Exchange newMerchantExchange(){
        return new TopicExchange("new_merchant_exchange",true,false);
    }

    /**
     * 创建普通队列
     * @return
     */
    @Bean
    public Queue newMerchantQueue(){

        Map<String,Object> args = new HashMap<>(3);
        //消息过期后，进入到死信交换机
        args.put("x-dead-letter-exchange","lock_merchant_dead_exchange");

        //消息过期后，进入到死信交换机的路由key
        args.put("x-dead-letter-routing-key","lock_merchant_routing_key");

        //过期时间，单位毫秒
        args.put("x-message-ttl",10000);

        return QueueBuilder.durable("new_merchant_queue").withArguments(args).build();
    }

    /**
     * 绑定交换机和队列
     * @return
     */
    @Bean
    public Binding newMerchantBinding(){

        return new Binding("new_merchant_queue",
                Binding.DestinationType.QUEUE,
                "new_merchant_exchange",
                "new_merchant_routing_key",
                null);
    }
}

