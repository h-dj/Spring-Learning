package cn.hdj.modules.message.service.impl;

import cn.hdj.modules.api.domain.MessageSendRequest;
import cn.hdj.modules.message.MessageSendHandlerCompose;
import cn.hdj.modules.message.PreParamCheckServiceCompose;
import cn.hdj.modules.message.exception.MessageExceptionHandler;
import cn.hdj.modules.message.service.IMessageSendService;
import cn.hdj.modules.message.service.MessageFilterService;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author huangjiajian
 * @Date 2022/8/6 下午5:22
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Service
@Slf4j
public class MessageSendServiceImpl implements IMessageSendService {


    @Resource
    MessageExceptionHandler messageExceptionHandler;

    @Resource
    private PreParamCheckServiceCompose preParamCheckServiceCompose;

    @Resource
    private MessageFilterService messageFilterService;

    @Resource
    private MessageSendHandlerCompose messageSendHandlerCompose;

    @Override
    public void send(MessageSendRequest request) {
        try {
            //参数校验
            preParamCheckServiceCompose.check(request);
            //黑名单过滤
            MessageSendRequest sendRequest = messageFilterService.filter(request);

            //幂等性处理

            //发送消息
            messageSendHandlerCompose.send(request);

            // 成功处理
        } catch (Throwable throwable) {
            //失败处理
            messageExceptionHandler.handler(request, ExceptionUtil.wrapRuntime(throwable));
        }
    }
}
