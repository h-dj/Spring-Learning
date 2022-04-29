package cn.hdj.message.message.handler;

import cn.hdj.message.message.domain.EmailMessageRequest;
import cn.hdj.message.message.domain.MessageRequest;

import javax.mail.MessagingException;

/**
 * @Description: 默认的参数处理器
 * @Author huangjiajian
 * @Date 2022/2/25 10:20
 */
public class SendMessageHandler implements MessageHandler {

    @Override
    public boolean match(MessageRequest request) {
        return EmailMessageRequest.class.isAssignableFrom(request.getClass());
    }

    @Override
    public void handler(MessageRequest request) throws MessagingException {

    }
}
