package cn.reid.springbatchdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_enrollment")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_no", nullable = false, length = 20)
    private String studentNo;

    @Column(name = "course_code", nullable = false, length = 20)
    private String courseCode;

    @Column(name = "semester", nullable = false, length = 20)
    private String semester;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "final_grade", length = 2)
    private String finalGrade;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @Column(name = "total_attendance")
    private Integer totalAttendance;

    @Column(name = "actual_attendance")
    private Integer actualAttendance;

    @Column(name = "dropped_reason", length = 200)
    private String droppedReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
