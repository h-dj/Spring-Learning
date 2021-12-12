package cn.hdj.repeatsubmit.config;

import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/12/11 下午6:18
 */
@MapperScan(basePackages = "cn.hdj.repeatsubmit.mapper")  //扫描mapper接口
@Configuration
public class MyBatisPlusConfig {
}
