package cn.hdj.demo_kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class DemoKafkaApplication {


    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DemoKafkaApplication.class, args);

        String ddddd = applicationContext.getEnvironment().getProperty("ddddd");
        log.info("=================ddddd: {}", ddddd);

    }

}
