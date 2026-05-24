package cn.reid.springbatchdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "t_exam_score")
public class ExamScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_no", nullable = false, length = 20)
    private String studentNo;

    @Column(name = "course_code", nullable = false, length = 20)
    private String courseCode;

    @Column(name = "score", precision = 5, scale = 1)
    private BigDecimal score;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "exam_type", length = 20)
    private String examType;

    @Column(name = "credit_points", precision = 3, scale = 1)
    private BigDecimal creditPoints;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "passed", length = 1)
    private String passed;

    @Column(name = "comments", length = 200)
    private String comments;

    @Column(name = "graded_by", length = 50)
    private String gradedBy;
}
