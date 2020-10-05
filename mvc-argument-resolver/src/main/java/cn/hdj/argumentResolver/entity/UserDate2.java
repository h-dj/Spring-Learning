package cn.hdj.argumentResolver.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author hdj
 * @version 1.0
 * @date 7/1/20 11:07 AM
 * @description:
 */
@Data
public class UserDate2 {

    private Date birthday;

    private String name;
}
