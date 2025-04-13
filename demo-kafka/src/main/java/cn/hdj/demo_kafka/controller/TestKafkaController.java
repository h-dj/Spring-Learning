package cn.hdj.demo_kafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestKafkaController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/send")
    public String send(@RequestParam("msg") String msg) {
        kafkaTemplate.send("test", msg);
        return "ok";
    }

    @GetMapping("/sendDelay")
    public String sendDelay(@RequestParam("msg") String msg) {
        kafkaTemplate.send("test", msg);
        return "ok";
    }
}
