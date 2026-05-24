package cn.reid.springbatchdemo.dto;

import lombok.Data;

@Data
public class CourseDTO {

    private Long id;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String courseType;
    private String department;
    private String teacher;
    private Integer maxStudents;
    private Integer hours;
    private String status;
    private String description;
}
