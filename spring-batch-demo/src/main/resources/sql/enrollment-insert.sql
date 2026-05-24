INSERT INTO t_enrollment (student_no, course_code, semester, enrollment_date, status, final_grade, attendance_rate, total_attendance, actual_attendance, dropped_reason, created_at)
VALUES (:studentNo, :courseCode, :semester, :enrollmentDate, :status, :finalGrade, :attendanceRate, :totalAttendance, :actualAttendance, :droppedReason, :createdAt)
ON DUPLICATE KEY UPDATE
    course_code      = :courseCode,
    semester         = :semester,
    enrollment_date  = :enrollmentDate,
    status           = :status,
    final_grade      = :finalGrade,
    attendance_rate  = :attendanceRate,
    total_attendance = :totalAttendance,
    actual_attendance = :actualAttendance,
    dropped_reason   = :droppedReason,
    created_at       = :createdAt
