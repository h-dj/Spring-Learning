package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.Enrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentFieldSetMapperTest {

    private static final String[] NAMES = {
            "studentNo", "courseCode", "semester", "enrollmentDate",
            "status", "finalGrade", "attendanceRate", "totalAttendance",
            "actualAttendance", "droppedReason", "createdAt"
    };

    private EnrollmentFieldSetMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EnrollmentFieldSetMapper();
    }

    @Test
    @DisplayName("11 个正确字段解析成功")
    void shouldMapAllFieldsCorrectly() throws BindException {
        String[] values = {"TEST001", "CS101", "2024-S1", "2024-03-01",
                "ENROLLED", "B", "95.00", "20", "19", "", "2024-03-01T10:00:00"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Enrollment enrollment = mapper.mapFieldSet(fieldSet);

        assertEquals("TEST001", enrollment.getStudentNo());
        assertEquals("CS101", enrollment.getCourseCode());
        assertEquals("2024-S1", enrollment.getSemester());
        assertEquals(LocalDate.of(2024, 3, 1), enrollment.getEnrollmentDate());
        assertEquals("ENROLLED", enrollment.getStatus());
        assertEquals("B", enrollment.getFinalGrade());
        assertEquals(new BigDecimal("95.00"), enrollment.getAttendanceRate());
        assertEquals(20, enrollment.getTotalAttendance());
        assertEquals(19, enrollment.getActualAttendance());
        assertNull(enrollment.getDroppedReason());
        assertEquals(LocalDateTime.of(2024, 3, 1, 10, 0, 0), enrollment.getCreatedAt());
    }

    @Test
    @DisplayName("enrollmentDate 格式非法时返回 null，其余字段不受影响")
    void shouldReturnNullEnrollmentDateWhenFormatInvalid() throws BindException {
        String[] values = {"TEST001", "CS101", "2024-S1", "not-a-date",
                "ENROLLED", "B", "95.00", "20", "19", "", "2024-03-01T10:00:00"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Enrollment enrollment = mapper.mapFieldSet(fieldSet);

        assertNull(enrollment.getEnrollmentDate());
        assertEquals("TEST001", enrollment.getStudentNo());
    }

    @Test
    @DisplayName("BigDecimal 字段 (attendanceRate) 为空白时返回 null")
    void shouldReturnNullBigDecimalWhenBlank() throws BindException {
        String[] values = {"TEST001", "CS101", "2024-S1", "2024-03-01",
                "ENROLLED", "B", "", "20", "19", "", "2024-03-01T10:00:00"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Enrollment enrollment = mapper.mapFieldSet(fieldSet);

        assertNull(enrollment.getAttendanceRate());
    }

    @Test
    @DisplayName("createdAt 为空白时返回 null（系统自动管理）")
    void shouldReturnNullCreatedAtWhenBlank() throws BindException {
        String[] values = {"TEST001", "CS101", "2024-S1", "2024-03-01",
                "ENROLLED", "B", "95.00", "20", "19", "", ""};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Enrollment enrollment = mapper.mapFieldSet(fieldSet);

        assertNull(enrollment.getCreatedAt());
    }

    @Test
    @DisplayName("Integer 字段 (totalAttendance) 为空白时返回 null")
    void shouldReturnNullIntegerWhenBlank() throws BindException {
        String[] values = {"TEST001", "CS101", "2024-S1", "2024-03-01",
                "ENROLLED", "B", "95.00", "", "", "", "2024-03-01T10:00:00"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Enrollment enrollment = mapper.mapFieldSet(fieldSet);

        assertNull(enrollment.getTotalAttendance());
        assertNull(enrollment.getActualAttendance());
    }

    @Test
    @DisplayName("DroppedReason 非空时正常解析")
    void shouldMapDroppedReasonWhenNotEmpty() throws BindException {
        String[] values = {"TEST001", "CS101", "2024-S1", "2024-03-01",
                "DROPPED", "", "50.00", "10", "5", "个人原因退课", "2024-03-01T10:00:00"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Enrollment enrollment = mapper.mapFieldSet(fieldSet);

        assertEquals("个人原因退课", enrollment.getDroppedReason());
    }
}
