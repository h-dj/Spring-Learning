package cn.hdj.demo_kafka.listener;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class TestKafkaListener {

    @KafkaListener(id = "myListener", topics = "test", autoStartup = "true")
    public void listen(String data) throws InterruptedException {
//        TimeUnit.SECONDS.sleep(new Random().nextInt(1));
        log.info("topic:test， msg:{} {}", data, DateUtil.format(new Date(), DatePattern.NORM_DATETIME_MS_PATTERN));
    }

}
