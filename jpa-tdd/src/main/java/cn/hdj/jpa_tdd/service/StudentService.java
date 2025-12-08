package cn.hdj.jpa_tdd.service;

import cn.hdj.jpa_tdd.entity.Student;

import java.util.List;

public interface StudentService {

    public Student addStudent(Student s);
    public Student assignCourse(Long studentId, Long courseId);
    public List<Student> getByClassName(String className);
}
