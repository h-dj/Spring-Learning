package cn.hdj.modules.message.service;

import cn.hdj.modules.api.domain.MessageSendRequest;

/**
 * @Author huangjiajian
 * @Date 2022/8/7 下午10:30
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
public interface MessageSendHandler {

    boolean support(MessageSendRequest request);

    void send(MessageSendRequest request);
}
