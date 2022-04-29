package cn.hdj.message.message.handler;

import cn.hdj.message.message.domain.BaseMessageRequest;
import cn.hdj.message.message.domain.EmailMessageRequest;
import cn.hdj.message.message.domain.MessageRequest;
import cn.hdj.message.message.enums.ChannelTypeEnum;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;

import java.util.Date;

/**
 * @Description: 默认的参数处理器
 * @Author huangjiajian
 * @Date 2022/2/25 10:20
 */
public class DefaultParamMessageHandler implements MessageHandler {

    @Override
    public boolean match(MessageRequest request) {
        return true;
    }

    @Override
    public void handler(MessageRequest request) {
        BaseMessageRequest baseMessageRequest = (BaseMessageRequest) request;
        //设置请求ID
        if (baseMessageRequest.getRequestId() == null) {
            baseMessageRequest.setRequestId(IdUtil.fastSimpleUUID());
        }
    }
}
