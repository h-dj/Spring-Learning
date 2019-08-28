package cn.hdj.entity;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hdj
 * @Description:
 * @date 8/23/19
 */
public class Teacher {


    public Teacher() {
        System.out.println("----Teacher 初始化----");
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + name + '\'' +
                '}';
    }
}
