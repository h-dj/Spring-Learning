package cn.hdj.demo_kafka;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
public class KafkaTest {

    @Resource(name = "stringTemplate")
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void testSend() {
        kafkaTemplate.send("test","aaa");
    }
}
