package cn.hdj.scene.controller;

import cn.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

/**
 * @Description: 使用  bitmap 实现签到功能
 * @Author huangjiajian
 * @Date 2022/2/2 下午10:29
 */
@RestController
@RequestMapping(value = "/signs")
public class UserSignController {


    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 签到
     */
    @PutMapping(value = "/sign")
    public void sign(Date date){
        //当前用户
        Long userId = getUserId();
        //当前签到年月
        String yearMonth = DateUtil.date(date).toString("yyyyMM");
        //当月 签到的日期天 offset
        String day = DateUtil.date(date).toString("dd");

        String key = String.format("user:sign:%d:%s",userId,yearMonth);

        this.redisTemplate
                .opsForValue()
                .setBit(key,Math.max(0,Long.parseLong(day)-1),true);
    }

    /**
     * 是否签到
     */
    @GetMapping(value = "/check")
    public Boolean check(Date date){

        //当前用户
        Long userId = getUserId();
        //当前签到年月
        String yearMonth = DateUtil.date(date).toString("yyyyMM");
        //当月 签到的日期天 offset
        String day = DateUtil.date(date).toString("dd");

        String key = String.format("user:sign:%d:%s",userId,yearMonth);

        return this.redisTemplate
                .opsForValue()
                .getBit(key,Math.max(0,Long.parseLong(day)-1));
    }

    /**
     * 获取给定月份  用户签到次数
     */
    @GetMapping(value = "/count")
    public Long getSignCount(Date date){

        //当前用户
        Long userId = getUserId();
        //当前签到年月
        String yearMonth = DateUtil.date(date).toString("yyyyMM");
        //当月 签到的日期天 offset
        String day = DateUtil.date(date).toString("dd");

        String key = String.format("user:sign:%d:%s",userId,yearMonth);

        return this.redisTemplate
                .execute(new RedisCallback<Long>() {
                    @Override
                    public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                        return redisConnection.bitCount(key.getBytes(StandardCharsets.UTF_8));
                    }
                });
    }


    /**
     *  获取当月连续签到次数
     */
    @GetMapping(value = "/count/continuous")
    public  List<Long> getContinuousSignCount(Date date){
        //当前用户
        Long userId = getUserId();
        //当前签到年月
        String yearMonth = DateUtil.date(date).toString("yyyyMM");
        //当月 签到的日期天 offset
        String day = DateUtil.date(date).toString("dd");
        final String key = String.format("user:sign:%d:%s",userId,yearMonth);

        BitSet bitSet = redisTemplate.execute(new RedisCallback<BitSet>() {
            @Override
            public BitSet doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return fromByteArrayReverse(redisConnection.get(key.getBytes(StandardCharsets.UTF_8)));
            }
        });

        //TODO  还没完成
        return null;

    }


    public Long getUserId(){
        return 10001L;
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
