package cn.reid.springbatchdemo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentDTO {

    private Long id;
    private String studentNo;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String email;
    private String className;
    private Integer enrollmentYear;
    private String status;
}
