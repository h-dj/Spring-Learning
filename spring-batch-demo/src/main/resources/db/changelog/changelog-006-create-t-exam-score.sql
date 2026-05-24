-- liquibase formatted sql
-- changeset reid:7
CREATE TABLE t_exam_score (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_no VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    score DECIMAL(5,1),
    exam_date DATE,
    exam_type VARCHAR(20),
    credit_points DECIMAL(3,1),
    rank INT,
    passed VARCHAR(1),
    comments VARCHAR(200),
    graded_by VARCHAR(50)
);
