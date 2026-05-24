package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.ExamScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class ExamScoreProcessor implements ItemProcessor<ExamScore, ExamScore> {

    private static final Logger log = LoggerFactory.getLogger(ExamScoreProcessor.class);

    private static final Set<String> VALID_EXAM_TYPES = Set.of("FINAL", "MIDTERM", "QUIZ", "MAKEUP");

    @Override
    public ExamScore process(ExamScore item) {
        // 1. student_no 不能为空
        if (item.getStudentNo() == null || item.getStudentNo().isBlank()) {
            log.warn("过滤记录: student_no 为空");
            return null;
        }

        // 2. course_code 不能为空
        if (item.getCourseCode() == null || item.getCourseCode().isBlank()) {
            log.warn("过滤记录: course_code 为空, student_no={}", item.getStudentNo());
            return null;
        }

        // 3. score 非空时校验范围 0-100
        if (item.getScore() != null) {
            if (item.getScore().compareTo(BigDecimal.ZERO) < 0
                    || item.getScore().compareTo(new BigDecimal("100")) > 0) {
                log.warn("过滤记录: score 超出范围 [0,100], score={}, student_no={}",
                        item.getScore(), item.getStudentNo());
                return null;
            }
        }

        // 4. exam_type 校验
        if (item.getExamType() != null) {
            if (item.getExamType().isBlank()) {
                log.warn("过滤记录: exam_type 为空白, student_no={}", item.getStudentNo());
                return null;
            }
            String upper = item.getExamType().toUpperCase();
            if (!VALID_EXAM_TYPES.contains(upper)) {
                log.warn("过滤记录: 非法 exam_type={}, student_no={}", item.getExamType(), item.getStudentNo());
                return null;
            }
            item.setExamType(upper);
        }

        // 5. passed 校验
        if (item.getPassed() != null) {
            if (item.getPassed().isBlank()) {
                log.warn("过滤记录: passed 为空白, student_no={}", item.getStudentNo());
                return null;
            }
            String upper = item.getPassed().toUpperCase();
            if (!"Y".equals(upper) && !"N".equals(upper)) {
                log.warn("过滤记录: 非法 passed={}, student_no={}", item.getPassed(), item.getStudentNo());
                return null;
            }
            item.setPassed(upper);
        }

        return item;
    }
}
