package cn.hdj.mvninterceptor;

import cn.hdj.mvninterceptor.interceptor.LogInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@SpringBootApplication
public class MvnInterceptorApplication implements ApplicationContextAware {

    private static ApplicationContext context;
    public static void main(String[] args) {
        SpringApplication.run(MvnInterceptorApplication.class, args);
        LogInterceptor bean = context.getBean(LogInterceptor.class);
        System.out.println(bean.toString());


    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
