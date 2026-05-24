package cn.reid.springbatchdemo.processor;

import cn.reid.springbatchdemo.entity.ClassGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ClassGroupProcessor implements ItemProcessor<ClassGroup, ClassGroup> {

    private static final Logger log = LoggerFactory.getLogger(ClassGroupProcessor.class);
    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "GRADUATED", "DISSOLVED");

    @Override
    public ClassGroup process(ClassGroup item) {
        // 1. 班级编号不能为空
        if (item.getClassNo() == null || item.getClassNo().isBlank()) {
            log.warn("过滤记录: class_no 为空, class_name={}", item.getClassName());
            return null;
        }

        // 2. 班级名称不能为空
        if (item.getClassName() == null || item.getClassName().isBlank()) {
            log.warn("过滤记录: class_name 为空, class_no={}", item.getClassNo());
            return null;
        }

        // 3. 专业不能为空
        if (item.getMajor() == null || item.getMajor().isBlank()) {
            log.warn("过滤记录: major 为空, class_no={}", item.getClassNo());
            return null;
        }

        // 4. status 校验（合法值：ACTIVE/GRADUATED/DISSOLVED，大小写不敏感）
        if (item.getStatus() != null && !item.getStatus().isBlank()) {
            String upper = item.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(upper)) {
                log.warn("过滤记录: 非法 status={}, class_no={}", item.getStatus(), item.getClassNo());
                return null;
            }
            item.setStatus(upper);
        }

        return item;
    }
}
