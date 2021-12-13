package cn.hdj.rediscache.controller;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @Description: 缓存穿透 --- 请求数据库不存在的值
 * 1、方案一，对于不存在的数据，同样设置一个特殊的 Value 到缓存中
 * 2、方案二，采用布隆过滤器，过滤无效的请求
 * 3、方案一、方案二同用
 * @Author huangjiajian
 * @Date 2021/9/16 11:17
 */
//@RestController
//@RequestMapping(value = "/cachepenetration")
public class CachePenetrationController {

    public static final Logger log = LoggerFactory.getLogger(CachePenetrationController.class);

    private AtomicInteger atomicInteger = new AtomicInteger();

    private BloomFilter<Integer> bloomFilter;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;




    @PostConstruct
    public void init() {
        //创建布隆过滤器，元素数量10000，期望误判率1%
        bloomFilter = BloomFilter.create(Funnels.integerFunnel(), 10000, 0.01);
        //填充布隆过滤器
        IntStream.rangeClosed(1, 10000).forEach(bloomFilter::put);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            log.info("DB QPS : {}", atomicInteger.getAndSet(0));
        }, 0, 1, TimeUnit.SECONDS);
    }

    @GetMapping("right")
    public String right(@RequestParam("id") int id) {
        String key = "user" + id;
        String data = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(data)) {
            data = getCityFromDb(id);
            //校验从数据库返回的数据是否有效
            if (!StringUtils.isEmpty(data)) {
                stringRedisTemplate.opsForValue().set(key, data, 30, TimeUnit.SECONDS);
            }
            else {
                //如果无效，直接在缓存中设置一个NODATA，这样下次查询时即使是无效用户还是可以命中缓存
                stringRedisTemplate.opsForValue().set(key, "NODATA", 30, TimeUnit.SECONDS);
            }
        }
        return data;
    }

    @GetMapping("right2")
    public String right2(@RequestParam("id") int id) {
        String data = "";
        //通过布隆过滤器先判断
        if (bloomFilter.mightContain(id)) {
            String key = "user" + id;
            //走缓存查询
            data = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isEmpty(data)) {
                //走数据库查询
                data = getCityFromDb(id);
                stringRedisTemplate.opsForValue().set(key, data, 30, TimeUnit.SECONDS);
            }
        }
        return data;
    }

    private String getCityFromDb(int cityId) {
        //模拟查询数据库，查一次增加计数器加一
        atomicInteger.incrementAndGet();
        return "citydata" + System.currentTimeMillis();
    }
}
