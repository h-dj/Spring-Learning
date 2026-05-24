package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.ClassGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClassGroupProcessor 单元测试")
class ClassGroupProcessorTest {

    private final ClassGroupProcessor processor = new ClassGroupProcessor();

    private ClassGroup createValidClassGroup() {
        ClassGroup group = new ClassGroup();
        group.setClassNo("CS101");
        group.setClassName("计算机科学一班");
        group.setGrade(1);
        group.setMajor("计算机科学与技术");
        group.setDepartment("计算机学院");
        group.setHeadTeacher("张教授");
        group.setStudentCount(45);
        group.setClassroom("A101");
        group.setBuilding("主教学楼");
        group.setEnrollmentYear(2024);
        group.setStatus("ACTIVE");
        return group;
    }

    @Test
    @DisplayName("classNo 为空时应返回 null")
    void shouldReturnNullWhenClassNoIsBlank() {
        ClassGroup group = createValidClassGroup();
        group.setClassNo("");
        assertNull(processor.process(group));

        group.setClassNo(null);
        assertNull(processor.process(group));

        group.setClassNo("   ");
        assertNull(processor.process(group));
    }

    @Test
    @DisplayName("className 为空时应返回 null")
    void shouldReturnNullWhenClassNameIsBlank() {
        ClassGroup group = createValidClassGroup();
        group.setClassName("");
        assertNull(processor.process(group));

        group.setClassName(null);
        assertNull(processor.process(group));
    }

    @Test
    @DisplayName("major 为空时应返回 null")
    void shouldReturnNullWhenMajorIsBlank() {
        ClassGroup group = createValidClassGroup();
        group.setMajor("");
        assertNull(processor.process(group));

        group.setMajor(null);
        assertNull(processor.process(group));
    }

    @Test
    @DisplayName("非法的 status 应返回 null")
    void shouldReturnNullWhenStatusIsInvalid() {
        ClassGroup group = createValidClassGroup();
        group.setStatus("INVALID");
        assertNull(processor.process(group));

        group.setStatus("unknown");
        assertNull(processor.process(group));

        group.setStatus("PENDING");
        assertNull(processor.process(group));
    }

    @Test
    @DisplayName("合法的 status（大小写不敏感）应转为大写并保留")
    void shouldAcceptValidStatusCaseInsensitive() {
        ClassGroup group = createValidClassGroup();

        group.setStatus("active");
        ClassGroup result = processor.process(group);
        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());

        group.setStatus("Graduated");
        result = processor.process(group);
        assertNotNull(result);
        assertEquals("GRADUATED", result.getStatus());

        group.setStatus("dissolved");
        result = processor.process(group);
        assertNotNull(result);
        assertEquals("DISSOLVED", result.getStatus());
    }

    @Test
    @DisplayName("所有字段有效时应返回非空对象")
    void shouldReturnNonNullWhenAllFieldsValid() {
        ClassGroup group = createValidClassGroup();
        ClassGroup result = processor.process(group);

        assertNotNull(result);
        assertEquals("CS101", result.getClassNo());
        assertEquals("计算机科学一班", result.getClassName());
        assertEquals(1, result.getGrade());
        assertEquals("计算机科学与技术", result.getMajor());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("带空格的空白字段也应被过滤")
    void shouldFilterWhitespaceOnlyFields() {
        ClassGroup group = createValidClassGroup();
        group.setClassNo("   ");
        assertNull(processor.process(group));

        group.setClassNo("CS102");
        group.setClassName("   ");
        assertNull(processor.process(group));

        group.setClassName("数学一班");
        group.setMajor("   ");
        assertNull(processor.process(group));
    }
}
