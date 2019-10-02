package cn.hdj.mvninterceptor.config;

import cn.hdj.mvninterceptor.filter.OneFilter;
import cn.hdj.mvninterceptor.filter.SecondFilter;
import cn.hdj.mvninterceptor.interceptor.LogInterceptor;
import cn.hdj.mvninterceptor.interceptor.ValidateInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
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

    @Bean
    public LogInterceptor logInterceptor(){
        return new LogInterceptor();
    }

    /**
     * 添加自定义拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(logInterceptor())//添加拦截器
                .addPathPatterns("/*") //拦截uri
                .order(1); //执行顺序

        registry.addInterceptor(new ValidateInterceptor())
                .addPathPatterns("/*")
                .order(2);
    }

    /**
     * 自定义过滤器
     */
//    @Bean
//    public FilterRegistrationBean oneFilter() {
//        FilterRegistrationBean oneFilter = new FilterRegistrationBean();
//        oneFilter.setFilter(new OneFilter());
//        oneFilter.setEnabled(true); //自动注册到Servlet 过滤链中
//        oneFilter.addUrlPatterns("/*");//拦截的请求
//        oneFilter.setOrder(1);//执行顺序
//        return oneFilter;
//    }

    @Bean
    public FilterRegistrationBean secondFilter() {
        FilterRegistrationBean secondFilter = new FilterRegistrationBean();
        secondFilter.setFilter(new SecondFilter());
        secondFilter.setEnabled(true);
        secondFilter.addUrlPatterns("/*");
        secondFilter.setOrder(0);
        return secondFilter;
    }
}
