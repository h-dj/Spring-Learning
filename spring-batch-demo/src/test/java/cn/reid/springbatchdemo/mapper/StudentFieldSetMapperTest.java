package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;

import java.time.LocalDate;

import org.springframework.validation.BindException;

import static org.junit.jupiter.api.Assertions.*;

class StudentFieldSetMapperTest {

    private static final String[] NAMES = {
            "studentNo", "name", "gender", "birthDate",
            "phone", "email", "className", "enrollmentYear", "status"
    };

    private StudentFieldSetMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StudentFieldSetMapper();
    }

    @Test
    @DisplayName("9 个正确字段解析成功")
    void shouldMapAllFieldsCorrectly() throws BindException {
        String[] values = {"10001", "张三", "M", "2000-01-15", "13800138000",
                "zhangsan@email.com", "计算机一班", "2023", "ACTIVE"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Student student = mapper.mapFieldSet(fieldSet);

        assertEquals("10001", student.getStudentNo());
        assertEquals("张三", student.getName());
        assertEquals("M", student.getGender());
        assertEquals(LocalDate.of(2000, 1, 15), student.getBirthDate());
        assertEquals("13800138000", student.getPhone());
        assertEquals("zhangsan@email.com", student.getEmail());
        assertEquals("计算机一班", student.getClassName());
        assertEquals(2023, student.getEnrollmentYear());
        assertEquals("ACTIVE", student.getStatus());
    }

    @Test
    @DisplayName("birthDate 格式非法时返回 null，其余字段不受影响")
    void shouldReturnNullBirthDateWhenFormatInvalid() throws BindException {
        String[] values = {"10001", "张三", "M", "not-a-date", "13800138000",
                "zhangsan@email.com", "计算机一班", "2023", "ACTIVE"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Student student = mapper.mapFieldSet(fieldSet);

        assertNull(student.getBirthDate());
        assertEquals("10001", student.getStudentNo());
        assertEquals("张三", student.getName());
    }
}
