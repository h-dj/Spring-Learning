package cn.hdj.jpa_tdd.service;

import cn.hdj.jpa_tdd.entity.Course;
import cn.hdj.jpa_tdd.entity.Student;
import cn.hdj.jpa_tdd.repository.CourseRepository;
import cn.hdj.jpa_tdd.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public Student addStudent(Student s) {
        // 可以在这里做必要的校验（非空、长度等）
        // 简单实现：直接保存并返回
        return studentRepository.save(s);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student assignCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("course not found"));

        student.getCourses().add(course);
        return studentRepository.save(student);
    }

    @Override
    public List<Student> getByClassName(String className) {
        return studentRepository.findByClassName(className);
    }
}