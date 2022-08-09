package cn.hdj.scene.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/8/9 11:22
 */
@RestController
@RequestMapping(value = "/redis/stream")
public class RedisStreamController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 添加消息用户组
     */
    @PostMapping(value = "/addGroup")
    public String addGroup(String streamName, String groupName, ReadOffset readOffset) {
        String group = null;
        //获取是否已有该名称 stream
        Long size = redisTemplate.opsForStream().size(streamName);
        if (size == null || size < 1) {
            // 没有设置读取规则
            group = redisTemplate.opsForStream().createGroup(streamName, readOffset, groupName);
        }
        if (size != null && size >= 1) {
            // 已有直接创建新的分组
            group = redisTemplate.opsForStream().createGroup(streamName, groupName);
        }
        return group;
    }


    /**
     * 添加消息
     *
     * @param streamName
     * @param map
     */
    @PostMapping(value = "/addMessage")
    public RecordId addMessage(String streamName, Map<Object, Object> map) {
        return this.redisTemplate.opsForStream()
                .add(streamName, map);
    }

    @GetMapping(value = "/readMessage")
    public List<MapRecord<String, Object, Object>> readMessage(String streamName, String groupName, String consumerName, Duration duration, ReadOffset readOffset) {
        List<MapRecord<String, Object, Object>> recordList = redisTemplate.opsForStream().read(
                // 创建消费者
                Consumer.from(groupName, consumerName),
                // 每次读取 1条消息
                StreamReadOptions.empty().count(1).block(duration),
                StreamOffset.create(streamName, readOffset)
        );
        if (recordList == null || recordList.isEmpty()) {
            return null;
        }
        return recordList;

    }

    @GetMapping(value = "/ack")
    public Long acknowledge(String streamName,String groupName, MapRecord<String, Object, Object> record) {
        return redisTemplate.opsForStream().acknowledge(streamName, groupName, record.getId());
    }
}
