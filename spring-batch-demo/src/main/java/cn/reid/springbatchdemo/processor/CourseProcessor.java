package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CourseProcessor implements ItemProcessor<Course, Course> {

    private static final Logger log = LoggerFactory.getLogger(CourseProcessor.class);
    private static final Set<String> VALID_COURSE_TYPES = Set.of("REQUIRED", "ELECTIVE", "PUBLIC");

    @Override
    public Course process(Course item) {
        // 1. courseCode 不能为空
        if (item.getCourseCode() == null || item.getCourseCode().isBlank()) {
            log.warn("过滤记录: course_code 为空, course_name={}", item.getCourseName());
            return null;
        }

        // 2. courseName 不能为空
        if (item.getCourseName() == null || item.getCourseName().isBlank()) {
            log.warn("过滤记录: course_name 为空, course_code={}", item.getCourseCode());
            return null;
        }

        // 3. courseType 必须为 REQUIRED/ELECTIVE/PUBLIC（大小写不敏感）
        if (item.getCourseType() == null || item.getCourseType().isBlank()) {
            log.warn("过滤记录: course_type 为空, course_code={}", item.getCourseCode());
            return null;
        }
        String upperType = item.getCourseType().toUpperCase();
        if (!VALID_COURSE_TYPES.contains(upperType)) {
            log.warn("过滤记录: 非法 course_type={}, course_code={}", item.getCourseType(), item.getCourseCode());
            return null;
        }
        item.setCourseType(upperType);

        // 4. status 统一转为大写
        if (item.getStatus() != null) {
            item.setStatus(item.getStatus().toUpperCase());
        }

        return item;
    }
}
