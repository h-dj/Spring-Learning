package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class StudentProcessor implements ItemProcessor<Student, Student> {

    private static final Logger log = LoggerFactory.getLogger(StudentProcessor.class);

    @Override
    public Student process(Student item) {
        // 1. 学号不能为空
        if (item.getStudentNo() == null || item.getStudentNo().isBlank()) {
            log.warn("过滤记录: student_no 为空, name={}", item.getName());
            return null;
        }

        // 2. 姓名不能为空
        if (item.getName() == null || item.getName().isBlank()) {
            log.warn("过滤记录: name 为空, student_no={}", item.getStudentNo());
            return null;
        }

        // 3. gender 只接受 M/F（大小写不敏感）
        if (item.getGender() != null && !item.getGender().isBlank()) {
            String upper = item.getGender().toUpperCase();
            if (!"M".equals(upper) && !"F".equals(upper)) {
                log.warn("过滤记录: 非法 gender={}, student_no={}", item.getGender(), item.getStudentNo());
                return null;
            }
            item.setGender(upper);
        }

        // 4. phone 非空时校验 11 位数字
        if (item.getPhone() != null && !item.getPhone().isBlank()) {
            String cleanPhone = item.getPhone().trim();
            if (!cleanPhone.matches("\\d{11}")) {
                log.warn("过滤记录: 非法 phone={}, student_no={}", item.getPhone(), item.getStudentNo());
                return null;
            }
            item.setPhone(cleanPhone);
        }

        // 5. status 统一转为大写
        if (item.getStatus() != null) {
            item.setStatus(item.getStatus().toUpperCase());
        }

        return item;
    }
}
