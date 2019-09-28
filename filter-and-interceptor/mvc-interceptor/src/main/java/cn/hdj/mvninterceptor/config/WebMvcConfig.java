package cn.hdj.mvninterceptor.config;

import cn.hdj.mvninterceptor.interceptor.LogInterceptor;
import cn.hdj.mvninterceptor.interceptor.ValidateInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author hdj
 * @Description:　mvc 配置
 * @date 9/28/19
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    /**
     * 添加自定义拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())//添加拦截器
                .addPathPatterns("/*") //拦截uri
                .order(1); //执行顺序

        registry.addInterceptor(new ValidateInterceptor())
                .addPathPatterns("/*")
                .order(2);
    }
}
