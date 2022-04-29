package cn.hdj.message.message.handler;

import cn.hdj.message.message.domain.BaseMessageRequest;
import cn.hdj.message.message.domain.EmailMessageRequest;
import cn.hdj.message.message.domain.MessageRequest;
import cn.hdj.message.message.domain.WebMsgRequest;
import cn.hdj.message.message.enums.ErrorCodeEnum;
import cn.hdj.message.message.exception.MessageException;
import cn.hutool.core.util.IdUtil;

/**
 * @Description: 发送前置参数检查
 * @Author huangjiajian
 * @Date 2022/2/25 10:20
 */
public class PreCheckMessageHandler implements MessageHandler {

    @Override
    public boolean match(MessageRequest request) {
        return true;
    }

    @Override
    public void handler(MessageRequest request) {
        if (request == null) {
            throw new MessageException(ErrorCodeEnum.REQUEST_OBJ_NULL);
        }

        BaseMessageRequest baseMessageRequest = (BaseMessageRequest) request;
        if (baseMessageRequest.getTemplateId() == null) {
            throw new MessageException(ErrorCodeEnum.TEMPLATE_ID_MISSING);
        }

        if (baseMessageRequest.getVarMap() == null) {
            throw new MessageException(ErrorCodeEnum.TEMPLATE_VARS_MISSING);
        }

        if(EmailMessageRequest.class.isAssignableFrom(request.getClass())){
            EmailMessageRequest emailMessageRequest = (EmailMessageRequest) request;
            if (emailMessageRequest.getFrom() == null) {
                throw new MessageException(ErrorCodeEnum.EMAIL_FROM_MISS);
            }
            if (emailMessageRequest.getToList() == null || emailMessageRequest.getToList().isEmpty()) {
                throw new MessageException(ErrorCodeEnum.EMAIL_FROM_MISS);
            }
        }

        if(WebMsgRequest.class.isAssignableFrom(request.getClass())){
            WebMsgRequest webMsgRequest = (WebMsgRequest) request;
            if (webMsgRequest.getSendUserId() == null) {
                throw new MessageException(ErrorCodeEnum.WEB_SEND_USER_MISS);
            }
            if (webMsgRequest.getReceiveUserIds() == null || webMsgRequest.getReceiveUserIds().isEmpty()) {
                throw new MessageException(ErrorCodeEnum.WEB_RECEIVE_USER_MISS);
            }
        }
    }
}
