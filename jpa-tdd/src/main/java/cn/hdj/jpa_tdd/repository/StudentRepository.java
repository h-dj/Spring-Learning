package cn.hdj.jpa_tdd.repository;

import cn.hdj.jpa_tdd.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByClassName(String className);

    @Query("select s from Student s join fetch s.courses where s.id = :id")
    Optional<Student> findWithCourses(Long id);
}