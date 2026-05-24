package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EnrollmentProcessor implements ItemProcessor<Enrollment, Enrollment> {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentProcessor.class);
    private static final Set<String> VALID_STATUSES = Set.of("ENROLLED", "DROPPED", "COMPLETED");
    private static final Set<String> VALID_GRADES = Set.of("A", "B", "C", "D", "F");

    @Override
    public Enrollment process(Enrollment item) {
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

        // 3. semester 不能为空
        if (item.getSemester() == null || item.getSemester().isBlank()) {
            log.warn("过滤记录: semester 为空, student_no={}", item.getStudentNo());
            return null;
        }

        // 4. status 校验：非空时必须为 ENROLLED/DROPPED/COMPLETED
        if (item.getStatus() != null && !item.getStatus().isBlank()) {
            String upper = item.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(upper)) {
                log.warn("过滤记录: 非法 status={}, student_no={}", item.getStatus(), item.getStudentNo());
                return null;
            }
            item.setStatus(upper);
        }

        // 5. final_grade 校验：非空时必须为 A/B/C/D/F
        if (item.getFinalGrade() != null && !item.getFinalGrade().isBlank()) {
            String upper = item.getFinalGrade().toUpperCase();
            if (!VALID_GRADES.contains(upper)) {
                log.warn("过滤记录: 非法 final_grade={}, student_no={}", item.getFinalGrade(), item.getStudentNo());
                return null;
            }
            item.setFinalGrade(upper);
        }

        return item;
    }
}
