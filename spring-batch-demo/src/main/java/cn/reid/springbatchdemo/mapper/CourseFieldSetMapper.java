package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class CourseFieldSetMapper implements FieldSetMapper<Course> {

    private static final Logger log = LoggerFactory.getLogger(CourseFieldSetMapper.class);
    private static final int EXPECTED_FIELD_COUNT = 10;

    @Override
    public Course mapFieldSet(FieldSet fieldSet) throws BindException {
        int fieldCount = fieldSet.getValues().length;
        if (fieldCount != EXPECTED_FIELD_COUNT) {
            String line = String.join("|", fieldSet.getValues());
            log.warn("跳过行: 字段数与 t_course 表不匹配 (期望={}, 实际={}), 内容={}",
                    EXPECTED_FIELD_COUNT, fieldCount, line);
            throw new IllegalArgumentException(String.format(
                    "字段数与 t_course 表不匹配: 期望 %d 列, 实际 %d 列", EXPECTED_FIELD_COUNT, fieldCount));
        }

        Course course = new Course();
        course.setCourseCode(fieldSet.readString("courseCode"));
        course.setCourseName(fieldSet.readString("courseName"));
        course.setCredits(readInt(fieldSet, "credits"));
        course.setCourseType(fieldSet.readString("courseType"));
        course.setDepartment(readOptionalString(fieldSet, "department"));
        course.setTeacher(readOptionalString(fieldSet, "teacher"));
        course.setMaxStudents(readIntOrNull(fieldSet, "maxStudents"));
        course.setHours(readIntOrNull(fieldSet, "hours"));
        course.setStatus(readOptionalString(fieldSet, "status"));
        course.setDescription(readOptionalString(fieldSet, "description"));
        return course;
    }

    private Integer readInt(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer readIntOrNull(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String readOptionalString(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
