package cn.hdj.message.message;

import cn.hdj.message.message.domain.TemplateContent;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/2/25 17:53
 */
public interface TemplateService {

    TemplateContent queryByTemplateId(String templateId);
}
