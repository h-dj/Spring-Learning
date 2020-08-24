package cn.hdj.provider.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午2:32
 * @description:
 */
@Data
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
    private String sex;
}
