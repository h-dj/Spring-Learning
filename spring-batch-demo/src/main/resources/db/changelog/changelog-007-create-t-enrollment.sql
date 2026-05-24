-- liquibase formatted sql
-- changeset reid:8
CREATE TABLE t_enrollment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_no VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    enrollment_date DATE,
    status VARCHAR(20),
    final_grade VARCHAR(2),
    attendance_rate DECIMAL(5,2),
    total_attendance INT,
    actual_attendance INT,
    dropped_reason VARCHAR(200),
    created_at TIMESTAMP
);
