package cn.hdj.client.config;

import feign.Logger;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/14 17:38
 */
@Slf4j
public class CustomFeignConfiguration {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    /**
     * 日志
     *
     * @return
     */
    @Bean
    public Logger.Level level() {
        // 让Feign打印所有请求的细节
        return Logger.Level.FULL;
    }


    /**
     * 加密编码器
     *
     * @return
     */
    @Bean
    public FeignRequestEncoder feignRequestEncoder() {
        return new FeignRequestEncoder(messageConverters);
    }

    /**
     * 解密编码器
     *
     * @return
     */
    @Bean
    public FeignResponseDecoder feignResponseDecoder(ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        return new FeignResponseDecoder(messageConverters, customizers);
    }


    /**
     * fegin 拦截器
     *
     * @return
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String value = request.getHeader(name);
                        /**
                         * 遍历请求头里面的属性字段，将logId和token添加到新的请求头中转发到下游服务
                         * */
                        if ("token".equalsIgnoreCase(name)) {
                            log.debug("添加自定义请求头key:" + name + ",value:" + value);
                            requestTemplate.header(name, value);
                        } else {
                            log.debug("FeignHeadConfiguration", "非自定义请求头key:" + name + ",value:" + value + "不需要添加!");
                        }
                    }
                } else {
                    log.warn("FeignHeadConfiguration", "获取请求头失败！");
                }
            }
        };
    }
}
