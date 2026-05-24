INSERT INTO t_teacher (teacher_no, name, gender, title, degree, department, phone, email, hire_date, salary_level, is_advisor, max_courses, status)
VALUES (:teacherNo, :name, :gender, :title, :degree, :department, :phone, :email, :hireDate, :salaryLevel, :isAdvisor, :maxCourses, :status)
ON DUPLICATE KEY UPDATE
    name            = :name,
    gender          = :gender,
    title           = :title,
    degree          = :degree,
    department      = :department,
    phone           = :phone,
    email           = :email,
    hire_date       = :hireDate,
    salary_level    = :salaryLevel,
    is_advisor      = :isAdvisor,
    max_courses     = :maxCourses,
    status          = :status
