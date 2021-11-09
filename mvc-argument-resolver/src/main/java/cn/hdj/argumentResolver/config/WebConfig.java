package cn.hdj.argumentResolver.config;


import cn.hdj.argumentResolver.entity.UserDate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.text.*;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;

/**
 * @author hdj
 * @version 1.0
 * @date 29/06/2020 23:35
 * @description:
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注册 Converters 和 Formatters
     *
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        //参数传出格式化
        registry.addFormatter(new DateFormatter("yyyy"));
    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                //指定时区
                .timeZone(TimeZone.getTimeZone("GMT+8:00"))
                //日期格式化
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        converters.add(0, new MappingJackson2HttpMessageConverter(builder.build()));
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {

    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {

    }
}
