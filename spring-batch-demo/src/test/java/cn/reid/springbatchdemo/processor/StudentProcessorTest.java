package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class StudentProcessorTest {

    private StudentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new StudentProcessor();
    }

    @Test
    @DisplayName("有效学生记录应正常通过处理")
    void shouldPassValidStudent() {
        Student student = createValidStudent();

        Student result = processor.process(student);

        assertNotNull(result);
        assertEquals("10001", result.getStudentNo());
        assertEquals("张三", result.getName());
    }

    @Test
    @DisplayName("学号为空时应返回 null（过滤）")
    void shouldFilterNullStudentNo() {
        Student student = createValidStudent();
        student.setStudentNo(null);

        assertNull(processor.process(student));
    }

    @Test
    @DisplayName("学号为空白时应返回 null（过滤）")
    void shouldFilterBlankStudentNo() {
        Student student = createValidStudent();
        student.setStudentNo("   ");

        assertNull(processor.process(student));
    }

    @Test
    @DisplayName("姓名为空时应返回 null（过滤）")
    void shouldFilterNullName() {
        Student student = createValidStudent();
        student.setName(null);

        assertNull(processor.process(student));
    }

    @Test
    @DisplayName("姓名为空白时应返回 null（过滤）")
    void shouldFilterBlankName() {
        Student student = createValidStudent();
        student.setName("   ");

        assertNull(processor.process(student));
    }

    @ParameterizedTest
    @CsvSource({
            "M, M",
            "F, F",
            "m, M",
            "f, F"
    })
    @DisplayName("有效 gender 值应通过处理并转为大写")
    void shouldAcceptValidGender(String input, String expected) {
        Student student = createValidStudent();
        student.setGender(input);

        Student result = processor.process(student);

        assertNotNull(result);
        assertEquals(expected, result.getGender());
    }

    @Test
    @DisplayName("非法 gender 值应返回 null（过滤）")
    void shouldFilterInvalidGender() {
        Student student = createValidStudent();
        student.setGender("X");

        assertNull(processor.process(student));
    }

    @Test
    @DisplayName("gender 为空时应通过（允许为空）")
    void shouldPassNullGender() {
        Student student = createValidStudent();
        student.setGender(null);

        assertNotNull(processor.process(student));
    }

    @Test
    @DisplayName("有效 11 位手机号应通过处理")
    void shouldPassValidPhone() {
        Student student = createValidStudent();
        student.setPhone("13800138000");

        Student result = processor.process(student);

        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
    }

    @Test
    @DisplayName("手机号为空时应通过（允许为空）")
    void shouldPassNullPhone() {
        Student student = createValidStudent();
        student.setPhone(null);

        assertNotNull(processor.process(student));
    }

    @Test
    @DisplayName("手机号为空字符串时应通过（允许为空）")
    void shouldPassEmptyPhone() {
        Student student = createValidStudent();
        student.setPhone("");

        assertNotNull(processor.process(student));
    }

    @Test
    @DisplayName("手机号不足 11 位时应返回 null（过滤）")
    void shouldFilterShortPhone() {
        Student student = createValidStudent();
        student.setPhone("1380013800");

        assertNull(processor.process(student));
    }

    @Test
    @DisplayName("手机号包含非数字字符时应返回 null（过滤）")
    void shouldFilterNonDigitPhone() {
        Student student = createValidStudent();
        student.setPhone("13800a13800");

        assertNull(processor.process(student));
    }

    @Test
    @DisplayName("status 应自动转为大写")
    void shouldConvertStatusToUpperCase() {
        Student student = createValidStudent();
        student.setStatus("active");

        Student result = processor.process(student);

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("status 为空时应通过（允许为空）")
    void shouldPassNullStatus() {
        Student student = createValidStudent();
        student.setStatus(null);

        assertNotNull(processor.process(student));
    }

    private Student createValidStudent() {
        Student student = new Student();
        student.setStudentNo("10001");
        student.setName("张三");
        student.setGender("M");
        student.setPhone("13800138000");
        student.setEmail("zhangsan@email.com");
        student.setClassName("计算机一班");
        student.setEnrollmentYear(2023);
        student.setStatus("ACTIVE");
        return student;
    }
}
