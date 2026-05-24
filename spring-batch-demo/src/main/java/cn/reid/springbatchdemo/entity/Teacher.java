package cn.reid.springbatchdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "t_teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_no", nullable = false, unique = true, length = 20)
    private String teacherNo;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "degree", length = 50)
    private String degree;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "salary_level")
    private Integer salaryLevel;

    @Column(name = "is_advisor", length = 1)
    private String isAdvisor;

    @Column(name = "max_courses")
    private Integer maxCourses;

    @Column(name = "status", length = 10)
    private String status;
}
