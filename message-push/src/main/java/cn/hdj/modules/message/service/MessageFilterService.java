package cn.hdj.modules.message.service;

import cn.hdj.modules.api.domain.MessageSendRequest;

/**
 * @Author huangjiajian
 * @Date 2022/8/7 下午4:00
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
public interface MessageFilterService {

    public MessageSendRequest filter(MessageSendRequest request);
}
