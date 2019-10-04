package cn.hdj.exampleTransaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hdj
 * @version 1.0
 * @date 2019/10/3 12:12
 * @description:
 */
@Configuration
public class Config {



    @Bean
    public UserRealm userRealm() {
        return new UserRealm();
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        SecurityManager securityManager = new SecurityManager(userRealm());
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        return shiroFilterFactoryBean;
    }
}
