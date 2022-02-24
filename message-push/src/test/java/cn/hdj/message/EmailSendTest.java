package cn.hdj.message;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2022/2/23 15:09
 */
public class EmailSendTest extends MessagePushApplicationTests{

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    public void test_send_email(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1432517356@qq.com");
        message.setTo("1272196984@qq.com");
        message.setSubject("测试");
        message.setText("发送邮件");
        javaMailSender.send(message);
    }
}
