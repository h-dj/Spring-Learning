INSERT INTO t_class_group (class_no, class_name, grade, major, department, head_teacher, student_count, classroom, building, enrollment_year, status)
VALUES (:classNo, :className, :grade, :major, :department, :headTeacher, :studentCount, :classroom, :building, :enrollmentYear, :status)
ON DUPLICATE KEY UPDATE
    class_name      = :className,
    grade           = :grade,
    major           = :major,
    department      = :department,
    head_teacher    = :headTeacher,
    student_count   = :studentCount,
    classroom       = :classroom,
    building        = :building,
    enrollment_year = :enrollmentYear,
    status          = :status
