package cn.hdj.modules.message.service;

import cn.hdj.modules.api.domain.MessageSendRequest;

/**
 * @Author huangjiajian
 * @Date 2022/8/7 下午3:41
 * @Description: 前置参数检验
 */
public interface PreParamCheckService {

    public void check(MessageSendRequest request);
}
