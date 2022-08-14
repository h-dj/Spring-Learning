package cn.hdj.modules.message;

import cn.hdj.modules.api.domain.MessageSendRequest;
import cn.hdj.modules.message.service.PreParamCheckService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author huangjiajian
 * @Date 2022/8/7 下午3:42
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Component
public class PreParamCheckServiceCompose {

    @Resource
    List<PreParamCheckService> preParamCheckServices;

    public void check(MessageSendRequest request) {
        for (PreParamCheckService checkService : preParamCheckServices) {
            checkService.check(request);
        }
    }
}
