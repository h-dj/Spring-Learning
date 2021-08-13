package cn.hdj.argumentResolver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class UserDate {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private Date birthday;

    private Date date;
}
