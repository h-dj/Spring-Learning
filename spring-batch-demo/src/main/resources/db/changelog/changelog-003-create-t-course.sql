-- liquibase formatted sql
-- changeset reid:4
CREATE TABLE t_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    department VARCHAR(50),
    teacher VARCHAR(50),
    max_students INT,
    hours INT,
    status VARCHAR(10) DEFAULT 'ACTIVE',
    description VARCHAR(500)
);
