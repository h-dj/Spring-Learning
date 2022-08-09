package cn.hdj.scene.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/8/8 11:31
 */
@RestController
@RequestMapping(value = "/api/redis/command")
public class CommandController {


    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping(value = "keys")
    public List<String> keys(@RequestParam String pattern) {
        RKeys keys = redissonClient.getKeys();
        return CollectionUtil.newArrayList(keys.getKeysByPattern(pattern));
    }

    @GetMapping(value = "keys2")
    public List<String> keys2(@RequestParam String pattern) {
        List<String> execute = stringRedisTemplate.execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
                List<String> list = new ArrayList<>();
                ScanOptions build = KeyScanOptions.scanOptions()
                        .count(10)
                        .type(DataType.STRING)
                        .match(pattern)
                        .match(pattern.getBytes(StandardCharsets.UTF_8))
                        .build();
                Cursor<byte[]> scan = connection.scan(build);
                while (scan.hasNext()) {
                    list.add(StrUtil.str(scan.next(), StandardCharsets.UTF_8));
                }
                scan.close();
                return list;
            }
        });
        return CollectionUtil.newArrayList(execute);
    }


    @GetMapping(value = "get")
    public Object get(String key) {
        return this.redissonClient.getBucket(key,new StringCodec()).get();
    }

}
