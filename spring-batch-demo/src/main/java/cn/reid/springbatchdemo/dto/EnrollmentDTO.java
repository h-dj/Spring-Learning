package cn.reid.springbatchdemo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EnrollmentDTO {

    private Long id;
    private String studentNo;
    private String courseCode;
    private String semester;
    private LocalDate enrollmentDate;
    private String status;
    private String finalGrade;
    private BigDecimal attendanceRate;
    private Integer totalAttendance;
    private Integer actualAttendance;
    private String droppedReason;
    private LocalDateTime createdAt;
}
