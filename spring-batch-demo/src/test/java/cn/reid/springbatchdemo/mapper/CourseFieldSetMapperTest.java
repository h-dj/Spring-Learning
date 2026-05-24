package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import static org.junit.jupiter.api.Assertions.*;

class CourseFieldSetMapperTest {

    private static final String[] NAMES = {
            "courseCode", "courseName", "credits", "courseType",
            "department", "teacher", "maxStudents", "hours", "status", "description"
    };

    private CourseFieldSetMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CourseFieldSetMapper();
    }

    @Test
    @DisplayName("10 个正确字段解析成功")
    void shouldMapAllFieldsCorrectly() throws BindException {
        String[] values = {"CS101", "计算机网络", "3", "REQUIRED", "计算机学院",
                "张教授", "60", "48", "ACTIVE", "计算机网络基础课程"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Course course = mapper.mapFieldSet(fieldSet);

        assertEquals("CS101", course.getCourseCode());
        assertEquals("计算机网络", course.getCourseName());
        assertEquals(Integer.valueOf(3), course.getCredits());
        assertEquals("REQUIRED", course.getCourseType());
        assertEquals("计算机学院", course.getDepartment());
        assertEquals("张教授", course.getTeacher());
        assertEquals(Integer.valueOf(60), course.getMaxStudents());
        assertEquals(Integer.valueOf(48), course.getHours());
        assertEquals("ACTIVE", course.getStatus());
        assertEquals("计算机网络基础课程", course.getDescription());
    }

    @Test
    @DisplayName("可选字段（department、teacher、maxStudents、hours、status、description）为空时应为 null")
    void shouldReturnNullForOptionalFieldsWhenEmpty() throws BindException {
        String[] values = {"CS101", "计算机网络", "3", "REQUIRED",
                "", "", "", "", "", ""};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Course course = mapper.mapFieldSet(fieldSet);

        assertEquals("CS101", course.getCourseCode());
        assertNull(course.getDepartment());
        assertNull(course.getTeacher());
        assertNull(course.getMaxStudents());
        assertNull(course.getHours());
        assertNull(course.getStatus());
        assertNull(course.getDescription());
    }

    @Test
    @DisplayName("credits 为 0 时应正确解析")
    void shouldMapZeroCredits() throws BindException {
        String[] values = {"CS101", "计算机网络", "0", "REQUIRED",
                "计算机学院", "张教授", "60", "48", "ACTIVE", ""};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Course course = mapper.mapFieldSet(fieldSet);

        assertEquals(Integer.valueOf(0), course.getCredits());
    }
}
