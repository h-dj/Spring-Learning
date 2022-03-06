package cn.hdj.mq.controller;

import cn.hdj.mq.config.RabbitTemplateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/3/5 下午8:19
 */
@RequestMapping(value = "/test")
@RestController
public class TestController {

    @Autowired
    private RabbitTemplateWrapper rabbitTemplateWrapper;

    @GetMapping("/send")
    public String test(){
        rabbitTemplateWrapper.convertAndSend("confirmTestExchange","","测试发送");
        return "ok";
    }
}
