package cn.reid.springbatchdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "t_student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_no", nullable = false, unique = true, length = 20)
    private String studentNo;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(name = "status", length = 20)
    private String status;
}
