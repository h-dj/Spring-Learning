package cn.hdj.argumentResolver;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.format.Formatter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.text.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * @author hdj
 * @version 1.0
 * @date 7/1/20 10:49 AM
 * @description:
 */
public class BaseController {

    /**
     * 初始化绑定参数user 标识前缀
     *
     * @param binder
     */
    @InitBinder("user")
    public void initBinderUser(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("user.");
    }

    /**
     * 初始化绑定参数friend 标识前缀
     *
     * @param binder
     */
    @InitBinder("friend")
    public void initBinderFriend(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("friend.");
    }


    /**
     * 注册日期转换 date
     *
     * @param binder
     */
    @InitBinder
    public void initBinderDate(WebDataBinder binder) {
        binder.addCustomFormatter(new Formatter<Date>() {
            @Override
            public Date parse(String text, Locale locale) throws ParseException {
                System.out.println("InitBinder addCustomFormatter String to Date  ");
                return new SimpleDateFormat("yyyy-MM-dd").parse(text);
            }

            @Override
            public String print(Date date, Locale locale) {
                System.out.println("InitBinder addCustomFormatter  Date to String  ");
                return new SimpleDateFormat("yyyy-MM-dd").format(date);
            }
        });
    }
}
