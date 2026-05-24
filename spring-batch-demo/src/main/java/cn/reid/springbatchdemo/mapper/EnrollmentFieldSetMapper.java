package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EnrollmentFieldSetMapper implements FieldSetMapper<Enrollment> {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentFieldSetMapper.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final int EXPECTED_FIELD_COUNT = 11;

    @Override
    public Enrollment mapFieldSet(FieldSet fieldSet) throws BindException {
        int fieldCount = fieldSet.getValues().length;
        if (fieldCount != EXPECTED_FIELD_COUNT) {
            String line = String.join("|", fieldSet.getValues());
            log.warn("跳过行: 字段数与 t_enrollment 表不匹配 (期望={}, 实际={}), 内容={}",
                    EXPECTED_FIELD_COUNT, fieldCount, line);
            throw new IllegalArgumentException(String.format(
                    "字段数与 t_enrollment 表不匹配: 期望 %d 列, 实际 %d 列", EXPECTED_FIELD_COUNT, fieldCount));
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentNo(fieldSet.readString("studentNo"));
        enrollment.setCourseCode(fieldSet.readString("courseCode"));
        enrollment.setSemester(fieldSet.readString("semester"));
        enrollment.setEnrollmentDate(readLocalDate(fieldSet, "enrollmentDate"));
        enrollment.setStatus(fieldSet.readString("status"));
        enrollment.setFinalGrade(fieldSet.readString("finalGrade"));
        enrollment.setAttendanceRate(readBigDecimal(fieldSet, "attendanceRate"));
        enrollment.setTotalAttendance(readInt(fieldSet, "totalAttendance"));
        enrollment.setActualAttendance(readInt(fieldSet, "actualAttendance"));
        enrollment.setDroppedReason(readStringOrNull(fieldSet, "droppedReason"));
        enrollment.setCreatedAt(readLocalDateTime(fieldSet, "createdAt"));
        return enrollment;
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

    private LocalDateTime readLocalDateTime(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal readBigDecimal(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer readInt(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String readStringOrNull(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
