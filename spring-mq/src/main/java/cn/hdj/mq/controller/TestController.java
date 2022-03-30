package cn.hdj.mq.controller;

import cn.hdj.mq.config.RabbitTemplateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/3/5 下午8:19
 */
@RequestMapping(value = "/test")
@RestController
@Slf4j
public class TestController {

    @Autowired
    private RabbitTemplateWrapper rabbitTemplateWrapper;

    @GetMapping("/send")
    public String test(){
        rabbitTemplateWrapper.convertAndSend("confirmTestExchange","","测试发送");
        return "ok";
    }

    @GetMapping("/send_delay")
    public String send_delay(){
        rabbitTemplateWrapper.convertAndSend(
                "new_merchant_exchange",
                "new_merchant_routing_key"                ,
                "测试延迟发送",
                "6000"
        );
        log.info("发送时间 {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return "ok";
    }
}
