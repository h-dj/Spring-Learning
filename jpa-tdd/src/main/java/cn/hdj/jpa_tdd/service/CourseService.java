package cn.hdj.jpa_tdd.service;

import cn.hdj.jpa_tdd.entity.Course;
import cn.hdj.jpa_tdd.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }
    
    public Optional<Course> findCourseById(Long id) {
        return courseRepository.findById(id);
    }
    
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }
    
    public void deleteCourseById(Long id) {
        courseRepository.deleteById(id);
    }

}