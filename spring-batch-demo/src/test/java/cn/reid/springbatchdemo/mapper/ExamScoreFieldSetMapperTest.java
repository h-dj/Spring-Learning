package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.entity.ExamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExamScoreFieldSetMapperTest {

    private ExamScoreFieldSetMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ExamScoreFieldSetMapper();
    }

    @Test
    @DisplayName("有效字段应正确映射到 ExamScore 实体")
    void shouldMapValidFields() throws Exception {
        FieldSet fieldSet = new DefaultFieldSet(new String[]{
                "10001", "CS101", "85.5", "2024-01-15",
                "FINAL", "3.0", "10", "Y", "Good work", "Dr. Smith"
        }, new String[]{
                "studentNo", "courseCode", "score", "examDate",
                "examType", "creditPoints", "rank", "passed", "comments", "gradedBy"
        });

        ExamScore result = mapper.mapFieldSet(fieldSet);

        assertNotNull(result);
        assertEquals("10001", result.getStudentNo());
        assertEquals("CS101", result.getCourseCode());
        assertEquals(new BigDecimal("85.5"), result.getScore());
        assertEquals(LocalDate.of(2024, 1, 15), result.getExamDate());
        assertEquals("FINAL", result.getExamType());
        assertEquals(new BigDecimal("3.0"), result.getCreditPoints());
        assertEquals(10, result.getRank());
        assertEquals("Y", result.getPassed());
        assertEquals("Good work", result.getComments());
        assertEquals("Dr. Smith", result.getGradedBy());
    }

    @Test
    @DisplayName("score 为空时应映射为 null")
    void shouldMapNullScore() throws Exception {
        FieldSet fieldSet = new DefaultFieldSet(new String[]{
                "10001", "CS101", "", "2024-01-15",
                "FINAL", "3.0", "", "", "", ""
        }, new String[]{
                "studentNo", "courseCode", "score", "examDate",
                "examType", "creditPoints", "rank", "passed", "comments", "gradedBy"
        });

        ExamScore result = mapper.mapFieldSet(fieldSet);

        assertNotNull(result);
        assertEquals("10001", result.getStudentNo());
        assertNull(result.getScore());
        assertNull(result.getRank());
        assertNull(result.getPassed());
    }

    @Test
    @DisplayName("exam_date 为空时应映射为 null")
    void shouldMapNullExamDate() throws Exception {
        FieldSet fieldSet = new DefaultFieldSet(new String[]{
                "10001", "CS101", "85.5", "",
                "FINAL", "3.0", "10", "Y", "", ""
        }, new String[]{
                "studentNo", "courseCode", "score", "examDate",
                "examType", "creditPoints", "rank", "passed", "comments", "gradedBy"
        });

        ExamScore result = mapper.mapFieldSet(fieldSet);

        assertNotNull(result);
        assertNull(result.getExamDate());
    }

    @Test
    @DisplayName("空字符串字段应映射为 null")
    void shouldMapBlankStringsToNull() throws Exception {
        FieldSet fieldSet = new DefaultFieldSet(new String[]{
                "10001", "CS101", "85.5", "2024-01-15",
                "", "", "10", "", "", ""
        }, new String[]{
                "studentNo", "courseCode", "score", "examDate",
                "examType", "creditPoints", "rank", "passed", "comments", "gradedBy"
        });

        ExamScore result = mapper.mapFieldSet(fieldSet);

        assertNotNull(result);
        assertNull(result.getExamType());
        assertNull(result.getCreditPoints());
        assertNull(result.getPassed());
        assertNull(result.getComments());
        assertNull(result.getGradedBy());
    }
}
