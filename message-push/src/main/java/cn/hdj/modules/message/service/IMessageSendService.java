package cn.hdj.modules.message.service;

import cn.hdj.modules.api.domain.MessageSendRequest;

/**
 * @Author huangjiajian
 * @Date 2022/8/6 下午5:21
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
public interface IMessageSendService {

    void send(MessageSendRequest request);
}
