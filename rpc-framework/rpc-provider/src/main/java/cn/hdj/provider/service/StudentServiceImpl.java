package cn.hdj.provider.service;

import cn.hdj.provider.entity.Student;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午2:34
 * @description:
 */
public class StudentServiceImpl implements StudentService {


    public Student getInfo() {
        Student person = new Student();
        person.setAge(25);
        person.setName("Java");
        person.setSex("男");
        return person;
    }

    public boolean printInfo(Student person) {
        if (person != null) {
            System.out.println(person);
            return true;
        }
        return false;
    }
}
