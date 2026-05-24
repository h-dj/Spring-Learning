package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CourseProcessorTest {

    private CourseProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CourseProcessor();
    }

    @Test
    @DisplayName("有效课程记录应正常通过处理")
    void shouldPassValidCourse() {
        Course course = createValidCourse();

        Course result = processor.process(course);

        assertNotNull(result);
        assertEquals("CS101", result.getCourseCode());
        assertEquals("计算机网络", result.getCourseName());
    }

    @Test
    @DisplayName("courseCode 为空时应返回 null（过滤）")
    void shouldFilterNullCourseCode() {
        Course course = createValidCourse();
        course.setCourseCode(null);

        assertNull(processor.process(course));
    }

    @Test
    @DisplayName("courseCode 为空白时应返回 null（过滤）")
    void shouldFilterBlankCourseCode() {
        Course course = createValidCourse();
        course.setCourseCode("   ");

        assertNull(processor.process(course));
    }

    @Test
    @DisplayName("courseName 为空时应返回 null（过滤）")
    void shouldFilterNullCourseName() {
        Course course = createValidCourse();
        course.setCourseName(null);

        assertNull(processor.process(course));
    }

    @Test
    @DisplayName("courseName 为空白时应返回 null（过滤）")
    void shouldFilterBlankCourseName() {
        Course course = createValidCourse();
        course.setCourseName("   ");

        assertNull(processor.process(course));
    }

    @Test
    @DisplayName("非法 courseType 应返回 null（过滤）")
    void shouldFilterInvalidCourseType() {
        Course course = createValidCourse();
        course.setCourseType("INVALID");

        assertNull(processor.process(course));
    }

    @Test
    @DisplayName("空 courseType 应返回 null（过滤）")
    void shouldFilterNullCourseType() {
        Course course = createValidCourse();
        course.setCourseType(null);

        assertNull(processor.process(course));
    }

    @ParameterizedTest
    @CsvSource({
            "REQUIRED, REQUIRED",
            "ELECTIVE, ELECTIVE",
            "PUBLIC, PUBLIC",
            "required, REQUIRED",
            "elective, ELECTIVE",
            "public, PUBLIC",
            "Required, REQUIRED"
    })
    @DisplayName("有效 courseType 值应通过处理并转为大写")
    void shouldAcceptValidCourseType(String input, String expected) {
        Course course = createValidCourse();
        course.setCourseType(input);

        Course result = processor.process(course);

        assertNotNull(result);
        assertEquals(expected, result.getCourseType());
    }

    @Test
    @DisplayName("status 应自动转为大写")
    void shouldConvertStatusToUpperCase() {
        Course course = createValidCourse();
        course.setStatus("active");

        Course result = processor.process(course);

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("status 为空时应通过（允许为空）")
    void shouldPassNullStatus() {
        Course course = createValidCourse();
        course.setStatus(null);

        Course result = processor.process(course);

        assertNotNull(result);
        assertNull(result.getStatus());
    }

    private Course createValidCourse() {
        Course course = new Course();
        course.setCourseCode("CS101");
        course.setCourseName("计算机网络");
        course.setCredits(3);
        course.setCourseType("REQUIRED");
        course.setDepartment("计算机学院");
        course.setTeacher("张教授");
        course.setMaxStudents(60);
        course.setHours(48);
        course.setStatus("ACTIVE");
        course.setDescription("计算机网络基础课程");
        return course;
    }
}
