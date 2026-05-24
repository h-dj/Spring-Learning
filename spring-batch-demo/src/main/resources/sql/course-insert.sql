INSERT INTO t_course (course_code, course_name, credits, course_type, department, teacher, max_students, hours, status, description)
VALUES (:courseCode, :courseName, :credits, :courseType, :department, :teacher, :maxStudents, :hours, :status, :description)
ON DUPLICATE KEY UPDATE
    course_name = :courseName, credits = :credits, course_type = :courseType, department = :department,
    teacher = :teacher, max_students = :maxStudents, hours = :hours, status = :status, description = :description
