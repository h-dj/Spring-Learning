package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.Teacher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TeacherProcessor implements ItemProcessor<Teacher, Teacher> {

    private static final Logger log = LoggerFactory.getLogger(TeacherProcessor.class);

    private static final Set<String> VALID_TITLES = Set.of("PROFESSOR", "ASSOCIATE", "LECTURER", "ASSISTANT");
    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "LEAVE", "RETIRED");

    @Override
    public Teacher process(Teacher item) {
        // 1. 教师编号不能为空
        if (item.getTeacherNo() == null || item.getTeacherNo().isBlank()) {
            log.warn("过滤记录: teacher_no 为空, name={}", item.getName());
            return null;
        }

        // 2. 姓名不能为空
        if (item.getName() == null || item.getName().isBlank()) {
            log.warn("过滤记录: name 为空, teacher_no={}", item.getTeacherNo());
            return null;
        }

        // 3. title 校验（非空时）：必须为有效值，转为大写
        if (item.getTitle() != null && !item.getTitle().isBlank()) {
            String upper = item.getTitle().toUpperCase();
            if (!VALID_TITLES.contains(upper)) {
                log.warn("过滤记录: 非法 title={}, teacher_no={}", item.getTitle(), item.getTeacherNo());
                return null;
            }
            item.setTitle(upper);
        }

        // 4. phone 非空时校验 11 位数字
        if (item.getPhone() != null && !item.getPhone().isBlank()) {
            String cleanPhone = item.getPhone().trim();
            if (!cleanPhone.matches("\\d{11}")) {
                log.warn("过滤记录: 非法 phone={}, teacher_no={}", item.getPhone(), item.getTeacherNo());
                return null;
            }
            item.setPhone(cleanPhone);
        }

        // 5. email 非空时校验必须包含 @
        if (item.getEmail() != null && !item.getEmail().isBlank()) {
            String cleanEmail = item.getEmail().trim();
            if (!cleanEmail.contains("@")) {
                log.warn("过滤记录: 非法 email={}, teacher_no={}", item.getEmail(), item.getTeacherNo());
                return null;
            }
            item.setEmail(cleanEmail);
        }

        // 6. status 校验（非空时）：必须为有效值，转为大写
        if (item.getStatus() != null && !item.getStatus().isBlank()) {
            String upper = item.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(upper)) {
                log.warn("过滤记录: 非法 status={}, teacher_no={}", item.getStatus(), item.getTeacherNo());
                return null;
            }
            item.setStatus(upper);
        }

        return item;
    }
}
