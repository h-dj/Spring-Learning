package cn.reid.springbatchdemo.dto;

import lombok.Data;

@Data
public class ClassGroupDTO {

    private Long id;
    private String classNo;
    private String className;
    private Integer grade;
    private String major;
    private String department;
    private String headTeacher;
    private Integer studentCount;
    private String classroom;
    private String building;
    private Integer enrollmentYear;
    private String status;
}
