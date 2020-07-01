package cn.hdj.argumentResolver.config;

import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        registry.addFormatter(new Formatter<Date>() {
            @Override
            public Date parse(String text, Locale locale) {
                System.out.println("解析字符串为 Date" + text);
                LocalDate parse = LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                Date from = Date.from(parse.atStartOfDay(ZoneId.systemDefault()).toInstant());
                return from;
            }

            @Override
            public String print(Date date, Locale locale) {
                System.out.println(" Date 格式化为 " + date);
                Instant instant = date.toInstant();
                LocalDate localDate = instant
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        });

    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .indentOutput(true)
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .modulesToInstall(new ParameterNamesModule());
        converters.add(0,new MappingJackson2HttpMessageConverter(builder.build()));
    }


    /**
     * 配置支持 MatrixVariable变量
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setRemoveSemicolonContent(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }
}
