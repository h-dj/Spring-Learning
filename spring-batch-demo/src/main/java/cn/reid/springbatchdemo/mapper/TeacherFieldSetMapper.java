package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.Teacher;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TeacherFieldSetMapper implements FieldSetMapper<Teacher> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Teacher mapFieldSet(FieldSet fieldSet) throws BindException {
        Teacher teacher = new Teacher();
        teacher.setTeacherNo(fieldSet.readString("teacherNo"));
        teacher.setName(fieldSet.readString("name"));
        teacher.setGender(fieldSet.readString("gender"));
        teacher.setTitle(fieldSet.readString("title"));
        teacher.setDegree(fieldSet.readString("degree"));
        teacher.setDepartment(fieldSet.readString("department"));
        teacher.setPhone(fieldSet.readString("phone"));
        teacher.setEmail(fieldSet.readString("email"));
        teacher.setHireDate(readLocalDate(fieldSet, "hireDate"));
        teacher.setSalaryLevel(fieldSet.readInt("salaryLevel", 0));
        teacher.setIsAdvisor(fieldSet.readString("isAdvisor"));
        teacher.setMaxCourses(fieldSet.readInt("maxCourses", 0));
        teacher.setStatus(fieldSet.readString("status"));
        return teacher;
    }

    private LocalDate readLocalDate(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
