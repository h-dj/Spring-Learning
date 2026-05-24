package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TeacherFieldSetMapperTest {

    private static final String[] NAMES = {
            "teacherNo", "name", "gender", "title", "degree", "department",
            "phone", "email", "hireDate", "salaryLevel", "isAdvisor", "maxCourses", "status"
    };

    private TeacherFieldSetMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TeacherFieldSetMapper();
    }

    @Test
    @DisplayName("14 个正确字段解析成功")
    void shouldMapAllFieldsCorrectly() throws BindException {
        String[] values = {"T001", "张教授", "M", "PROFESSOR", "博士", "计算机系",
                "13800138001", "zhang@edu.cn", "2010-09-01", "5", "Y", "5", "ACTIVE"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Teacher teacher = mapper.mapFieldSet(fieldSet);

        assertEquals("T001", teacher.getTeacherNo());
        assertEquals("张教授", teacher.getName());
        assertEquals("M", teacher.getGender());
        assertEquals("PROFESSOR", teacher.getTitle());
        assertEquals("博士", teacher.getDegree());
        assertEquals("计算机系", teacher.getDepartment());
        assertEquals("13800138001", teacher.getPhone());
        assertEquals("zhang@edu.cn", teacher.getEmail());
        assertEquals(LocalDate.of(2010, 9, 1), teacher.getHireDate());
        assertEquals(5, teacher.getSalaryLevel());
        assertEquals("Y", teacher.getIsAdvisor());
        assertEquals(5, teacher.getMaxCourses());
        assertEquals("ACTIVE", teacher.getStatus());
    }

    @Test
    @DisplayName("hireDate 格式非法时返回 null，其余字段不受影响")
    void shouldReturnNullHireDateWhenFormatInvalid() throws BindException {
        String[] values = {"T002", "李老师", "F", "ASSOCIATE", "硕士", "数学系",
                "13900139002", "li@edu.cn", "not-a-date", "4", "N", "3", "ACTIVE"};
        FieldSet fieldSet = new DefaultFieldSet(values, NAMES);

        Teacher teacher = mapper.mapFieldSet(fieldSet);

        assertNull(teacher.getHireDate());
        assertEquals("T002", teacher.getTeacherNo());
        assertEquals("李老师", teacher.getName());
    }
}
