package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TeacherProcessorTest {

    private TeacherProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TeacherProcessor();
    }

    @Test
    @DisplayName("有效教师记录应正常通过处理")
    void shouldPassValidTeacher() {
        Teacher teacher = createValidTeacher();

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("T001", result.getTeacherNo());
        assertEquals("张教授", result.getName());
    }

    @Test
    @DisplayName("教师编号为空时应返回 null（过滤）")
    void shouldFilterNullTeacherNo() {
        Teacher teacher = createValidTeacher();
        teacher.setTeacherNo(null);

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("教师编号为空白时应返回 null（过滤）")
    void shouldFilterBlankTeacherNo() {
        Teacher teacher = createValidTeacher();
        teacher.setTeacherNo("   ");

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("姓名为空时应返回 null（过滤）")
    void shouldFilterNullName() {
        Teacher teacher = createValidTeacher();
        teacher.setName(null);

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("姓名为空白时应返回 null（过滤）")
    void shouldFilterBlankName() {
        Teacher teacher = createValidTeacher();
        teacher.setName("   ");

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("有效 title 值应通过处理并转为大写")
    void shouldAcceptValidTitleUpperCase() {
        Teacher teacher = createValidTeacher();
        teacher.setTitle("professor");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("PROFESSOR", result.getTitle());
    }

    @Test
    @DisplayName("ASSOCIATE 应通过处理并转为大写")
    void shouldAcceptAssociateTitle() {
        Teacher teacher = createValidTeacher();
        teacher.setTitle("associate");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("ASSOCIATE", result.getTitle());
    }

    @Test
    @DisplayName("LECTURER 应通过处理并转为大写")
    void shouldAcceptLecturerTitle() {
        Teacher teacher = createValidTeacher();
        teacher.setTitle("lecturer");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("LECTURER", result.getTitle());
    }

    @Test
    @DisplayName("ASSISTANT 应通过处理并转为大写")
    void shouldAcceptAssistantTitle() {
        Teacher teacher = createValidTeacher();
        teacher.setTitle("assistant");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("ASSISTANT", result.getTitle());
    }

    @Test
    @DisplayName("非法 title 值应返回 null（过滤）")
    void shouldFilterInvalidTitle() {
        Teacher teacher = createValidTeacher();
        teacher.setTitle("INVALID_TITLE");

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("title 为空时应通过（允许为空）")
    void shouldPassNullTitle() {
        Teacher teacher = createValidTeacher();
        teacher.setTitle(null);

        assertNotNull(processor.process(teacher));
    }

    @Test
    @DisplayName("有效 11 位手机号应通过处理")
    void shouldPassValidPhone() {
        Teacher teacher = createValidTeacher();
        teacher.setPhone("13800138000");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
    }

    @Test
    @DisplayName("手机号为空时应通过（允许为空）")
    void shouldPassNullPhone() {
        Teacher teacher = createValidTeacher();
        teacher.setPhone(null);

        assertNotNull(processor.process(teacher));
    }

    @Test
    @DisplayName("手机号为空字符串时应通过（允许为空）")
    void shouldPassEmptyPhone() {
        Teacher teacher = createValidTeacher();
        teacher.setPhone("");

        assertNotNull(processor.process(teacher));
    }

    @Test
    @DisplayName("手机号不足 11 位时应返回 null（过滤）")
    void shouldFilterShortPhone() {
        Teacher teacher = createValidTeacher();
        teacher.setPhone("1380013800");

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("手机号包含非数字字符时应返回 null（过滤）")
    void shouldFilterNonDigitPhone() {
        Teacher teacher = createValidTeacher();
        teacher.setPhone("13800a13800");

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("有效电子邮件应通过处理")
    void shouldPassValidEmail() {
        Teacher teacher = createValidTeacher();
        teacher.setEmail("teacher@edu.cn");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("teacher@edu.cn", result.getEmail());
    }

    @Test
    @DisplayName("电子邮件不含 @ 时应返回 null（过滤）")
    void shouldFilterEmailWithoutAt() {
        Teacher teacher = createValidTeacher();
        teacher.setEmail("invalid-email");

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("电子邮件为空时应通过（允许为空）")
    void shouldPassNullEmail() {
        Teacher teacher = createValidTeacher();
        teacher.setEmail(null);

        assertNotNull(processor.process(teacher));
    }

    @Test
    @DisplayName("电子邮件为空字符串时应通过（允许为空）")
    void shouldPassEmptyEmail() {
        Teacher teacher = createValidTeacher();
        teacher.setEmail("");

        assertNotNull(processor.process(teacher));
    }

    @Test
    @DisplayName("有效 status 值应通过处理并转为大写")
    void shouldAcceptValidStatusUpperCase() {
        Teacher teacher = createValidTeacher();
        teacher.setStatus("active");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("LEAVE 应通过处理并转为大写")
    void shouldAcceptLeaveStatus() {
        Teacher teacher = createValidTeacher();
        teacher.setStatus("leave");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("LEAVE", result.getStatus());
    }

    @Test
    @DisplayName("RETIRED 应通过处理并转为大写")
    void shouldAcceptRetiredStatus() {
        Teacher teacher = createValidTeacher();
        teacher.setStatus("retired");

        Teacher result = processor.process(teacher);

        assertNotNull(result);
        assertEquals("RETIRED", result.getStatus());
    }

    @Test
    @DisplayName("非法 status 值应返回 null（过滤）")
    void shouldFilterInvalidStatus() {
        Teacher teacher = createValidTeacher();
        teacher.setStatus("INVALID_STATUS");

        assertNull(processor.process(teacher));
    }

    @Test
    @DisplayName("status 为空时应通过（允许为空）")
    void shouldPassNullStatus() {
        Teacher teacher = createValidTeacher();
        teacher.setStatus(null);

        assertNotNull(processor.process(teacher));
    }

    private Teacher createValidTeacher() {
        Teacher teacher = new Teacher();
        teacher.setTeacherNo("T001");
        teacher.setName("张教授");
        teacher.setGender("M");
        teacher.setTitle("PROFESSOR");
        teacher.setDegree("博士");
        teacher.setDepartment("计算机系");
        teacher.setPhone("13800138000");
        teacher.setEmail("zhang@edu.cn");
        teacher.setHireDate(LocalDate.of(2010, 9, 1));
        teacher.setSalaryLevel(5);
        teacher.setIsAdvisor("Y");
        teacher.setMaxCourses(5);
        teacher.setStatus("ACTIVE");
        return teacher;
    }
}
