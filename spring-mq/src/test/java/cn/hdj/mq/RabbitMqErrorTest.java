package cn.hdj.mq;

import cn.hdj.mq.config.RabbitTemplateWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description:  MQ 相关异常处理实践
 * @Author huangjiajian
 * @Date 2022/3/29 下午9:54
 */
public class RabbitMqErrorTest extends RabbitmqDemoApplicationTests{

    @Autowired
    private RabbitTemplateWrapper rabbitTemplateWrapper;

    @Test
    public void test_send_msg(){
        rabbitTemplateWrapper.convertAndSend("confirmTestExchange","","测试");
    }
}
