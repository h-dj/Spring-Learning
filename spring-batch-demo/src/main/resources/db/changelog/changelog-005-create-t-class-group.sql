-- liquibase formatted sql
-- changeset reid:6
CREATE TABLE t_class_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_no VARCHAR(20) NOT NULL UNIQUE,
    class_name VARCHAR(100) NOT NULL,
    grade INT NOT NULL,
    major VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    head_teacher VARCHAR(50),
    student_count INT,
    classroom VARCHAR(50),
    building VARCHAR(50),
    enrollment_year INT,
    status VARCHAR(10) DEFAULT 'ACTIVE'
);
