package cn.hdj.modules.message;

import cn.hdj.modules.api.domain.MessageSendRequest;
import cn.hdj.modules.message.service.MessageSendHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author huangjiajian
 * @Date 2022/8/7 下午10:31
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Component
public class MessageSendHandlerCompose {

    @Resource
    private List<MessageSendHandler> messageSendHandlers;

    /**
     * 发送消息
     *
     * @param request
     */
    public void send(MessageSendRequest request) {
        for (MessageSendHandler messageSendHandler : messageSendHandlers) {
            if (messageSendHandler.support(request)) {
                messageSendHandler.send(request);
            }
        }
    }
}
