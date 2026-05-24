package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.ExamScore;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExamScoreFieldSetMapper implements FieldSetMapper<ExamScore> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int EXPECTED_FIELD_COUNT = 10;

    @Override
    public ExamScore mapFieldSet(FieldSet fieldSet) throws BindException {
        if (fieldSet.getValues().length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "字段数与 t_exam_score 表不匹配: 期望 %d 列, 实际 %d 列",
                    EXPECTED_FIELD_COUNT, fieldSet.getValues().length));
        }

        ExamScore examScore = new ExamScore();
        examScore.setStudentNo(fieldSet.readString("studentNo"));
        examScore.setCourseCode(fieldSet.readString("courseCode"));
        examScore.setScore(readBigDecimal(fieldSet, "score"));
        examScore.setExamDate(readLocalDate(fieldSet, "examDate"));
        examScore.setExamType(readBlankableString(fieldSet, "examType"));
        examScore.setCreditPoints(readBigDecimal(fieldSet, "creditPoints"));
        examScore.setRank(fieldSet.readInt("rank", 0));
        if (examScore.getRank() == 0) {
            examScore.setRank(null);
        }
        examScore.setPassed(readBlankableString(fieldSet, "passed"));
        examScore.setComments(readBlankableString(fieldSet, "comments"));
        examScore.setGradedBy(readBlankableString(fieldSet, "gradedBy"));
        return examScore;
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

    private BigDecimal readBigDecimal(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String readBlankableString(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
