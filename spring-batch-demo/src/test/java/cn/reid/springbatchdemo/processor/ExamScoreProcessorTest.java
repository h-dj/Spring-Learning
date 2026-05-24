package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.ExamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExamScoreProcessorTest {

    private ExamScoreProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ExamScoreProcessor();
    }

    @Test
    @DisplayName("有效成绩记录应正常通过处理")
    void shouldPassValidExamScore() {
        ExamScore item = createValidExamScore();

        ExamScore result = processor.process(item);

        assertNotNull(result);
        assertEquals("10001", result.getStudentNo());
        assertEquals("CS101", result.getCourseCode());
    }

    @Test
    @DisplayName("学号为空时应返回 null")
    void shouldFilterNullStudentNo() {
        ExamScore item = createValidExamScore();
        item.setStudentNo(null);

        assertNull(processor.process(item));
    }

    @Test
    @DisplayName("学号为空白时应返回 null")
    void shouldFilterBlankStudentNo() {
        ExamScore item = createValidExamScore();
        item.setStudentNo("   ");

        assertNull(processor.process(item));
    }

    @Test
    @DisplayName("课程代码为空时应返回 null")
    void shouldFilterNullCourseCode() {
        ExamScore item = createValidExamScore();
        item.setCourseCode(null);

        assertNull(processor.process(item));
    }

    @Test
    @DisplayName("课程代码为空白时应返回 null")
    void shouldFilterBlankCourseCode() {
        ExamScore item = createValidExamScore();
        item.setCourseCode("   ");

        assertNull(processor.process(item));
    }

    @Test
    @DisplayName("score 为 null 时应通过处理")
    void shouldPassNullScore() {
        ExamScore item = createValidExamScore();
        item.setScore(null);

        assertNotNull(processor.process(item));
    }

    @Test
    @DisplayName("score 小于 0 时应返回 null")
    void shouldFilterScoreBelowZero() {
        ExamScore item = createValidExamScore();
        item.setScore(new BigDecimal("-1"));

        assertNull(processor.process(item));
    }

    @Test
    @DisplayName("score 大于 100 时应返回 null")
    void shouldFilterScoreAboveHundred() {
        ExamScore item = createValidExamScore();
        item.setScore(new BigDecimal("100.5"));

        assertNull(processor.process(item));
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "50",
            "100"
    })
    @DisplayName("score 在 0-100 范围内应通过处理")
    void shouldPassValidScore(BigDecimal score) {
        ExamScore item = createValidExamScore();
        item.setScore(score);

        ExamScore result = processor.process(item);

        assertNotNull(result);
        assertEquals(score, result.getScore());
    }

    @Test
    @DisplayName("score 为 0 时应通过处理")
    void shouldPassZeroScore() {
        ExamScore item = createValidExamScore();
        item.setScore(BigDecimal.ZERO);

        assertNotNull(processor.process(item));
    }

    @Test
    @DisplayName("exam_type 为 null 时应通过处理")
    void shouldPassNullExamType() {
        ExamScore item = createValidExamScore();
        item.setExamType(null);

        ExamScore result = processor.process(item);

        assertNotNull(result);
        assertNull(result.getExamType());
    }

    @Test
    @DisplayName("exam_type 为空白时应返回 null")
    void shouldFilterBlankExamType() {
        ExamScore item = createValidExamScore();
        item.setExamType("   ");

        assertNull(processor.process(item));
    }

    @Test
    @DisplayName("无效 exam_type 应返回 null")
    void shouldFilterInvalidExamType() {
        ExamScore item = createValidExamScore();
        item.setExamType("UNKNOWN");

        assertNull(processor.process(item));
    }

    @ParameterizedTest
    @CsvSource({
            "final, FINAL",
            "FINAL, FINAL",
            "midterm, MIDTERM",
            "MIDTERM, MIDTERM",
            "quiz, QUIZ",
            "QUIZ, QUIZ",
            "makeup, MAKEUP",
            "MAKEUP, MAKEUP"
    })
    @DisplayName("有效 exam_type 应通过处理并转为大写")
    void shouldAcceptValidExamType(String input, String expected) {
        ExamScore item = createValidExamScore();
        item.setExamType(input);

        ExamScore result = processor.process(item);

        assertNotNull(result);
        assertEquals(expected, result.getExamType());
    }

    @Test
    @DisplayName("passed 为 null 时应通过处理")
    void shouldPassNullPassed() {
        ExamScore item = createValidExamScore();
        item.setPassed(null);

        ExamScore result = processor.process(item);

        assertNotNull(result);
        assertNull(result.getPassed());
    }

    @Test
    @DisplayName("passed 为空白时应返回 null")
    void shouldFilterBlankPassed() {
        ExamScore item = createValidExamScore();
        item.setPassed("   ");

        assertNull(processor.process(item));
    }

    @ParameterizedTest
    @CsvSource({
            "y, Y",
            "Y, Y",
            "n, N",
            "N, N"
    })
    @DisplayName("有效 passed 值应通过处理并转为大写")
    void shouldAcceptValidPassed(String input, String expected) {
        ExamScore item = createValidExamScore();
        item.setPassed(input);

        ExamScore result = processor.process(item);

        assertNotNull(result);
        assertEquals(expected, result.getPassed());
    }

    @Test
    @DisplayName("无效 passed 值应返回 null")
    void shouldFilterInvalidPassed() {
        ExamScore item = createValidExamScore();
        item.setPassed("X");

        assertNull(processor.process(item));
    }

    private ExamScore createValidExamScore() {
        ExamScore item = new ExamScore();
        item.setStudentNo("10001");
        item.setCourseCode("CS101");
        item.setScore(new BigDecimal("85.5"));
        item.setExamDate(java.time.LocalDate.of(2024, 1, 15));
        item.setExamType("FINAL");
        item.setCreditPoints(new BigDecimal("3.0"));
        item.setRank(10);
        item.setPassed("Y");
        item.setComments("Good work");
        item.setGradedBy("Dr. Smith");
        return item;
    }
}
