package cn.hdj.mq.config;

import lombok.ToString;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/3/31 15:30
 */
@ConfigurationProperties(
        prefix = "spring.rabbitmq"
)
@Configuration
@Primary
@ToString(callSuper = true)
public class RabbitProperties2 extends RabbitProperties {

    String test2;

    public String getTest2() {
        return test2;
    }

    public void setTest2(String test2) {
        this.test2 = test2;
    }


}
