package cn.hdj.demo_kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class DemoKafkaApplication {


    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DemoKafkaApplication.class, args);
        log.info("wwwww");

        KafkaTemplate<String, String> kafkaTemplate = applicationContext.getBean("stringTemplate", KafkaTemplate.class);
//        for (long i = 0; i < 100000000000L; i++) {
//            kafkaTemplate.send("test", "hello" + i);
//        }
        TimeUnit.DAYS.sleep(1);
    }

}
