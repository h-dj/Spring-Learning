package cn.hdj.rediscache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RedisCacheApplicationTests {


    @Autowired
    RedissonClient redisson;


    @Test
    void contextLoads() throws JsonProcessingException {
        Map map = new HashMap();
        map.put("a", "測試");

        RBlockingQueue<Object> queue = this.redisson.getBlockingQueue("test");
        RDelayedQueue<Object> delayedQueue = this.redisson.getDelayedQueue(queue);
        delayedQueue.offer(map, 1, TimeUnit.DAYS);
    }

}
