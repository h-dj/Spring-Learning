package cn.hdj.argumentResolver.entity;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author hdj
 * @version 1.0
 * @date 29/06/2020 21:50
 * @description:
 */
@Data
@XmlRootElement(name ="user")
public class User {
    Integer id;
    String name;
}
