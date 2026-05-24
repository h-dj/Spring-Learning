package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.ClassGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClassGroupFieldSetMapper 单元测试")
class ClassGroupFieldSetMapperTest {

    private final ClassGroupFieldSetMapper mapper = new ClassGroupFieldSetMapper();

    @Test
    @DisplayName("有效字段集应正确映射为 ClassGroup 对象")
    void shouldMapValidFieldSet() throws Exception {
        FieldSet fieldSet = new DefaultFieldSet(new String[]{
                "CS101", "计算机科学一班", "1", "计算机科学与技术",
                "计算机学院", "张教授", "45", "A101", "主教学楼", "2024", "ACTIVE"
        }, new String[]{
                "classNo", "className", "grade", "major",
                "department", "headTeacher", "studentCount", "classroom", "building", "enrollmentYear", "status"
        });

        ClassGroup result = mapper.mapFieldSet(fieldSet);

        assertNotNull(result);
        assertEquals("CS101", result.getClassNo());
        assertEquals("计算机科学一班", result.getClassName());
        assertEquals(1, result.getGrade());
        assertEquals("计算机科学与技术", result.getMajor());
        assertEquals("计算机学院", result.getDepartment());
        assertEquals("张教授", result.getHeadTeacher());
        assertEquals(45, result.getStudentCount());
        assertEquals("A101", result.getClassroom());
        assertEquals("主教学楼", result.getBuilding());
        assertEquals(2024, result.getEnrollmentYear());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("字段数量不匹配时应抛出异常")
    void shouldThrowExceptionWhenFieldCountMismatch() {
        FieldSet fieldSet = new DefaultFieldSet(new String[]{
                "CS101", "计算机科学一班", "1"
        }, new String[]{
                "classNo", "className", "grade"
        });

        assertThrows(IllegalArgumentException.class, () -> mapper.mapFieldSet(fieldSet));
    }

    @Test
    @DisplayName("可为空的字段为空时应映射为 null")
    void shouldMapNullableFieldsToNullWhenEmpty() throws Exception {
        FieldSet fieldSet = new DefaultFieldSet(new String[]{
                "CS102", "计算机科学二班", "2", "软件工程",
                "", "", "", "", "", "2023", "ACTIVE"
        }, new String[]{
                "classNo", "className", "grade", "major",
                "department", "headTeacher", "studentCount", "classroom", "building", "enrollmentYear", "status"
        });

        ClassGroup result = mapper.mapFieldSet(fieldSet);

        assertNotNull(result);
        assertNull(result.getDepartment());
        assertNull(result.getHeadTeacher());
        assertNull(result.getStudentCount());
        assertNull(result.getClassroom());
        assertNull(result.getBuilding());
    }
}
