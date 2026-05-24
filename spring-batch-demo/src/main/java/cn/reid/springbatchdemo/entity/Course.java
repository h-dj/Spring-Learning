package cn.reid.springbatchdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", nullable = false, unique = true, length = 20)
    private String courseCode;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name = "course_type", nullable = false, length = 20)
    private String courseType;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "teacher", length = 50)
    private String teacher;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "hours")
    private Integer hours;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "description", length = 500)
    private String description;
}
