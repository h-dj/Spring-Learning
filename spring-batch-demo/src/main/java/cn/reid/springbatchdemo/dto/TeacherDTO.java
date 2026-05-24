package cn.reid.springbatchdemo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TeacherDTO {

    private Long id;
    private String teacherNo;
    private String name;
    private String gender;
    private String title;
    private String degree;
    private String department;
    private String phone;
    private String email;
    private LocalDate hireDate;
    private Integer salaryLevel;
    private String isAdvisor;
    private Integer maxCourses;
    private String status;
}
