INSERT INTO t_student (student_no, name, gender, birth_date, phone, email, class_name, enrollment_year, status)
VALUES (:studentNo, :name, :gender, :birthDate, :phone, :email, :className, :enrollmentYear, :status)
ON DUPLICATE KEY UPDATE
    name            = :name,
    gender          = :gender,
    birth_date      = :birthDate,
    phone           = :phone,
    email           = :email,
    class_name      = :className,
    enrollment_year = :enrollmentYear,
    status          = :status
