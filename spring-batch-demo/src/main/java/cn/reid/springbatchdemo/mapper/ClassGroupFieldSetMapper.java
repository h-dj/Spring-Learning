package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.ClassGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class ClassGroupFieldSetMapper implements FieldSetMapper<ClassGroup> {

    private static final Logger log = LoggerFactory.getLogger(ClassGroupFieldSetMapper.class);
    private static final int EXPECTED_FIELD_COUNT = 11;

    @Override
    public ClassGroup mapFieldSet(FieldSet fieldSet) throws BindException {
        int fieldCount = fieldSet.getValues().length;
        if (fieldCount != EXPECTED_FIELD_COUNT) {
            String line = String.join("|", fieldSet.getValues());
            log.warn("跳过行: 字段数与 t_class_group 表不匹配 (期望={}, 实际={}), 内容={}",
                    EXPECTED_FIELD_COUNT, fieldCount, line);
            throw new IllegalArgumentException(String.format(
                    "字段数与 t_class_group 表不匹配: 期望 %d 列, 实际 %d 列", EXPECTED_FIELD_COUNT, fieldCount));
        }

        ClassGroup group = new ClassGroup();
        group.setClassNo(fieldSet.readString("classNo"));
        group.setClassName(fieldSet.readString("className"));
        group.setGrade(fieldSet.readInt("grade", 0));
        group.setMajor(fieldSet.readString("major"));

        String department = fieldSet.readString("department");
        group.setDepartment(department.isBlank() ? null : department);

        String headTeacher = fieldSet.readString("headTeacher");
        group.setHeadTeacher(headTeacher.isBlank() ? null : headTeacher);

        group.setStudentCount(fieldSet.readInt("studentCount", 0));
        if (group.getStudentCount() == 0) {
            group.setStudentCount(null);
        }

        String classroom = fieldSet.readString("classroom");
        group.setClassroom(classroom.isBlank() ? null : classroom);

        String building = fieldSet.readString("building");
        group.setBuilding(building.isBlank() ? null : building);

        group.setEnrollmentYear(fieldSet.readInt("enrollmentYear", 0));
        if (group.getEnrollmentYear() == 0) {
            group.setEnrollmentYear(null);
        }

        group.setStatus(fieldSet.readString("status"));
        return group;
    }
}
