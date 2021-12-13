package cn.hdj.rediscache.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/9/15 17:32
 */
@Configuration
public class RedisConfig {


    @Bean
    public RedissonClient redissonClient(){
        // 1. Create config object
        Config config = new Config();
        config.useSingleServer()
                // use "rediss://" for SSL connection
                //.setPassword("123456")
                .setDatabase(0)
                .setAddress("redis://192.168.30.5:6379");
        config.setCodec(new JsonJacksonCodec());
        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }

}
