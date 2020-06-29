package cn.hdj.argumentResolver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
                return Date.from(parse.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
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

        //参数传入转换
        registry.addConverter(new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                System.out.println("字符串转换为Date " + source);
                return Date.from(ZonedDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd")).toInstant());
            }
        });


    }
}
