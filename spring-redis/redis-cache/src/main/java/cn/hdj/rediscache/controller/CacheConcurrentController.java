package cn.hdj.rediscache.controller;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: 注意缓存击穿问题 --  热点数据失效，导致数据库并发回源问题
 * 方案一，使用进程内的锁进行限制，这样每一个节点都可以以一个并发回源数据库；
 * 方案二，不使用锁进行限制，而是使用类似 Semaphore 的工具限制并发数，比如限制为 10，这样既限制了回源并发数不至于太大，又能使得一定量的线程可以同时回源。
 * @Author huangjiajian
 * @Date 2021/9/15 17:29
 */

@RequestMapping("cacheconcurrent")
@RestController
public class CacheConcurrentController {
    public static final Logger log = LoggerFactory.getLogger(CacheConcurrentController.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private AtomicInteger atomicInteger = new AtomicInteger();
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 初始化热点数据
     */
    //@PostConstruct
    public void init() {
        stringRedisTemplate.opsForValue().set("hotsopt", getExpensiveData(), 5, TimeUnit.SECONDS);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            log.info("DB QPS : {}", atomicInteger.getAndSet(0));
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 错误处理
     *
     * 重新缓存，没有加分布式锁，出现并发回源
     * @return
     */
    @GetMapping("wrong")
    public String wrong() {
        String data = stringRedisTemplate.opsForValue().get("hotsopt");
        if (StringUtils.isEmpty(data)) {
            data = getExpensiveData();
            stringRedisTemplate.opsForValue().set("hotsopt", data, 5, TimeUnit.SECONDS);
        }
        return data;
    }

    @GetMapping("right")
    public String right() {
        String data = stringRedisTemplate.opsForValue().get("hotsopt");
        if (StringUtils.isEmpty(data)) {
            RLock locker = redissonClient.getLock("locker");
            if (locker.tryLock()) {
                try {
                    data = stringRedisTemplate.opsForValue().get("hotsopt");
                    if (StringUtils.isEmpty(data)) {
                        data = getExpensiveData();
                        stringRedisTemplate.opsForValue().set("hotsopt", data, 5, TimeUnit.SECONDS);
                    }
                } finally {
                    locker.unlock();
                }
            }
        }
        return data;
    }

    private String getExpensiveData() {
        atomicInteger.incrementAndGet();
        return "important data";
    }
}
