package cn.hdj.scene.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * @Description: redis bitmap 实现
 * @Author cloud-inman
 * @Date 2021/11/9 15:00
 */
@Service
@Slf4j
public class BitMapsStatisticService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @PostConstruct
    public void init() {
        for (int i = 0; i < 16; i++) {
            if(i == 4){
                continue;
            }
            stringRedisTemplate.opsForValue()
                    .setBit("test", i, true);
        }

        Long execute = stringRedisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {

                return redisConnection.bitCount("test".getBytes(), 0, 1);
            }
        });
        System.out.println(execute);

        BitSet bitSet = stringRedisTemplate.execute(new RedisCallback<BitSet>() {
            @Override
            public BitSet doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return fromByteArrayReverse(redisConnection.get("test".getBytes()));
            }
        });
        System.out.println(bitSet.get(4));
    }


    /**
     * 解决bitSet java redis 字节顺序问题
     */
    private static BitSet fromByteArrayReverse(final byte[] bytes) {
        final BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[i / 8] & (1 << (7 - (i % 8)))) != 0) {
                bits.set(i);
            }
        }
        return bits;
    }
}
