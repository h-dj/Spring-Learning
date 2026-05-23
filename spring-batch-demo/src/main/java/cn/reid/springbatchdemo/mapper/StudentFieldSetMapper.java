package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StudentFieldSetMapper implements FieldSetMapper<Student> {

    private static final Logger log = LoggerFactory.getLogger(StudentFieldSetMapper.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int EXPECTED_FIELD_COUNT = 9;

    @Override
    public Student mapFieldSet(FieldSet fieldSet) throws BindException {
        int fieldCount = fieldSet.getValues().length;
        if (fieldCount != EXPECTED_FIELD_COUNT) {
            String line = String.join("|", fieldSet.getValues());
            log.warn("跳过行: 字段数与 t_student 表不匹配 (期望={}, 实际={}), 内容={}",
                    EXPECTED_FIELD_COUNT, fieldCount, line);
            throw new IllegalArgumentException(String.format(
                    "字段数与 t_student 表不匹配: 期望 %d 列, 实际 %d 列", EXPECTED_FIELD_COUNT, fieldCount));
        }

        Student student = new Student();
        student.setStudentNo(fieldSet.readString("studentNo"));
        student.setName(fieldSet.readString("name"));
        student.setGender(fieldSet.readString("gender"));
        student.setBirthDate(readLocalDate(fieldSet, "birthDate"));
        student.setPhone(fieldSet.readString("phone"));
        student.setEmail(fieldSet.readString("email"));
        student.setClassName(fieldSet.readString("className"));
        student.setEnrollmentYear(fieldSet.readInt("enrollmentYear", 0));
        student.setStatus(fieldSet.readString("status"));
        return student;
    }

    private LocalDate readLocalDate(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
