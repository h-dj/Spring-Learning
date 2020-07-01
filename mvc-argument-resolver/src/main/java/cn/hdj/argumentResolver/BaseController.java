package cn.hdj.argumentResolver;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
//    @InitBinder("date")
    public void initBinderDate(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                new DateFormat() {
                    @Override
                    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
                        Instant instant = date.toInstant();
                        LocalDate localDate = instant
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        return toAppendTo.append(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    }

                    @Override
                    public Date parse(String source, ParsePosition pos) {
                        pos.setIndex(1);
                        LocalDate parse = LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        return Date.from(parse.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    }
                }, true));
    }
}
