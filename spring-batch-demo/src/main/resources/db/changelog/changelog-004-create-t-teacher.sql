-- liquibase formatted sql
-- changeset reid:5
CREATE TABLE t_teacher (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_no VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(1),
    title VARCHAR(50),
    degree VARCHAR(50),
    department VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    hire_date DATE,
    salary_level INT,
    is_advisor VARCHAR(1),
    max_courses INT,
    status VARCHAR(10) DEFAULT 'ACTIVE'
);
