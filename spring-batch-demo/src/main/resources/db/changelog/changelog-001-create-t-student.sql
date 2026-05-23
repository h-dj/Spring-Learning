-- liquibase formatted sql
-- changeset reid:2

CREATE TABLE t_student (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_no      VARCHAR(20)  NOT NULL,
    name            VARCHAR(50)  NOT NULL,
    gender          VARCHAR(1),
    birth_date      DATE,
    phone           VARCHAR(20),
    email           VARCHAR(100),
    class_name      VARCHAR(50),
    enrollment_year INT,
    status          VARCHAR(20),
    CONSTRAINT uk_student_no UNIQUE (student_no)
);
