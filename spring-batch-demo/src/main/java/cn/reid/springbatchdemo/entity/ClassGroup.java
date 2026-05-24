package cn.reid.springbatchdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_class_group")
public class ClassGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_no", nullable = false, unique = true, length = 20)
    private String classNo;

    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

    @Column(name = "grade", nullable = false)
    private Integer grade;

    @Column(name = "major", nullable = false, length = 50)
    private String major;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "head_teacher", length = 50)
    private String headTeacher;

    @Column(name = "student_count")
    private Integer studentCount;

    @Column(name = "classroom", length = 50)
    private String classroom;

    @Column(name = "building", length = 50)
    private String building;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(name = "status", length = 10)
    private String status;
}
