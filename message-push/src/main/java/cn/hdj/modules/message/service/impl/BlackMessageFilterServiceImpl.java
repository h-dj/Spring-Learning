package cn.hdj.modules.message.service.impl;

import cn.hdj.modules.api.domain.MessageSendRequest;
import cn.hdj.modules.api.domain.Receiver;
import cn.hdj.modules.message.service.MessageFilterService;
import cn.hutool.core.bean.BeanUtil;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author huangjiajian
 * @Date 2022/8/7 下午4:00
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
public class BlackMessageFilterServiceImpl implements MessageFilterService {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public MessageSendRequest filter(MessageSendRequest request) {

        List<Receiver> receiverAddressList = request.getReceiverAddressList();

        List<Receiver> receiverList = receiverAddressList.stream()
                .filter(v -> {
                    Long userId = v.getUserId();
                    //web 站内信 黑名单过滤
                    RSet<Object> clientSet = redissonClient.getSet("msg:web:black:user");
                    if (clientSet.contains(userId)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        MessageSendRequest messageSendRequest = BeanUtil.copyProperties(request, MessageSendRequest.class);
        messageSendRequest.setReceiverAddressList(receiverList);
        return request;
    }
}
