package cn.reid.springbatchdemo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExamScoreDTO {

    private Long id;
    private String studentNo;
    private String courseCode;
    private BigDecimal score;
    private LocalDate examDate;
    private String examType;
    private BigDecimal creditPoints;
    private Integer rank;
    private String passed;
    private String comments;
    private String gradedBy;
}
