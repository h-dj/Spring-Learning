package cn.hdj.message.message;

import cn.hdj.message.message.domain.MessageRequest;
import cn.hdj.message.message.handler.MessageHandler;
import cn.hdj.message.message.sender.MessageSender;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 消息推送服务
 * @Author huangjiajian
 * @Date 2022/2/25 9:59
 */
@Slf4j
public class MessagePushService implements MessageSender {
    /**
     * 消息处理器
     */
    private List<MessageHandler> messageHandler;



    @Override
    public void send(MessageRequest messageRequest) throws Exception {
        List<MessageHandler> messageHandler = this.getMessageHandler();
        MessageHandler currentHandler = null;
        try {
            for (MessageHandler handler : messageHandler) {
                currentHandler = handler;
                if (handler.match(messageRequest)) {
                    handler.handler(messageRequest);
                }
            }
        } catch (Exception e) {
            log.error("消息处理错误 ：" + JSONUtil.toJsonStr(messageRequest), e);
            currentHandler.error(messageRequest, e);
        }
        log.info("发送消息 ：", JSONUtil.toJsonStr(messageRequest));
    }


    public List<MessageHandler> getMessageHandler() {
        if (messageHandler == null) {
            messageHandler = new ArrayList<>();
        }
        return messageHandler;
    }
}
