package cn.hdj.message.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 */
@MapperScan(basePackages = "cn.hdj.message.mapper")  //扫描mapper接口
@Configuration
public class MyBatisPlusConfig {
}
