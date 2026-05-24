package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.Enrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentProcessorTest {

    private EnrollmentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new EnrollmentProcessor();
    }

    @Test
    @DisplayName("有效选课记录应正常通过处理")
    void shouldPassValidEnrollment() {
        Enrollment enrollment = createValidEnrollment();
        Enrollment result = processor.process(enrollment);

        assertNotNull(result);
        assertEquals("TEST001", result.getStudentNo());
        assertEquals("CS101", result.getCourseCode());
        assertEquals("2024-S1", result.getSemester());
    }

    @Test
    @DisplayName("student_no 为空时应返回 null（过滤）")
    void shouldFilterNullStudentNo() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setStudentNo(null);

        assertNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("student_no 为空白时应返回 null（过滤）")
    void shouldFilterBlankStudentNo() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setStudentNo("   ");

        assertNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("course_code 为空时应返回 null（过滤）")
    void shouldFilterNullCourseCode() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setCourseCode(null);

        assertNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("course_code 为空白时应返回 null（过滤）")
    void shouldFilterBlankCourseCode() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setCourseCode("   ");

        assertNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("semester 为空时应返回 null（过滤）")
    void shouldFilterNullSemester() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setSemester(null);

        assertNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("semester 为空白时应返回 null（过滤）")
    void shouldFilterBlankSemester() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setSemester("   ");

        assertNull(processor.process(enrollment));
    }

    @ParameterizedTest
    @CsvSource({
            "ENROLLED, ENROLLED",
            "DROPPED, DROPPED",
            "COMPLETED, COMPLETED",
            "enrolled, ENROLLED",
            "Enrolled, ENROLLED",
            "completed, COMPLETED"
    })
    @DisplayName("有效 status 值应通过处理并转为大写")
    void shouldAcceptValidStatus(String input, String expected) {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setStatus(input);

        Enrollment result = processor.process(enrollment);

        assertNotNull(result);
        assertEquals(expected, result.getStatus());
    }

    @Test
    @DisplayName("非法 status 值应返回 null（过滤）")
    void shouldFilterInvalidStatus() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setStatus("UNKNOWN");

        assertNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("status 为空时应通过（允许为空）")
    void shouldPassNullStatus() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setStatus(null);

        assertNotNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("status 为空白时应通过（允许为空）")
    void shouldPassBlankStatus() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setStatus("   ");

        Enrollment result = processor.process(enrollment);
        assertNotNull(result);
        assertEquals("   ", result.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "A, A",
            "B, B",
            "C, C",
            "D, D",
            "F, F",
            "a, A",
            "b, B"
    })
    @DisplayName("有效 final_grade 值应通过处理并转为大写")
    void shouldAcceptValidFinalGrade(String input, String expected) {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setFinalGrade(input);

        Enrollment result = processor.process(enrollment);

        assertNotNull(result);
        assertEquals(expected, result.getFinalGrade());
    }

    @Test
    @DisplayName("非法 final_grade 值应返回 null（过滤）")
    void shouldFilterInvalidFinalGrade() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setFinalGrade("X");

        assertNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("final_grade 为空时应通过（允许为空）")
    void shouldPassNullFinalGrade() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setFinalGrade(null);

        assertNotNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("final_grade 为空白时应通过（允许为空）")
    void shouldPassBlankFinalGrade() {
        Enrollment enrollment = createValidEnrollment();
        enrollment.setFinalGrade("");

        assertNotNull(processor.process(enrollment));
    }

    @Test
    @DisplayName("有效记录中非校验字段应保持不变")
    void shouldPreserveOtherFields() {
        Enrollment enrollment = createValidEnrollment();

        Enrollment result = processor.process(enrollment);

        assertNotNull(result);
        assertEquals(new BigDecimal("95.00"), result.getAttendanceRate());
        assertEquals(20, result.getTotalAttendance());
        assertEquals(19, result.getActualAttendance());
    }

    private Enrollment createValidEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentNo("TEST001");
        enrollment.setCourseCode("CS101");
        enrollment.setSemester("2024-S1");
        enrollment.setStatus("ENROLLED");
        enrollment.setFinalGrade("B");
        enrollment.setAttendanceRate(new BigDecimal("95.00"));
        enrollment.setTotalAttendance(20);
        enrollment.setActualAttendance(19);
        return enrollment;
    }
}
