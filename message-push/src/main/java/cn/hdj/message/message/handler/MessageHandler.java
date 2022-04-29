package cn.hdj.message.message.handler;

import cn.hdj.message.message.domain.MessageRequest;
import cn.hdj.message.message.enums.ChannelTypeEnum;
import lombok.extern.slf4j.Slf4j;

import javax.mail.MessagingException;

/**
 * @Description: 消息处理器
 * @Author huangjiajian
 * @Date 2022/2/25 9:53
 */
public interface MessageHandler {

    /**
     * 匹配处理器
     *
     * @param request
     * @return
     */
    boolean match(MessageRequest request);

    /**
     * 处理器执行
     *
     * @param request
     */
    void handler(MessageRequest request) throws Exception;

    /**
     * 当前处理器失败
     *
     * @param request
     * @param throwable
     */
    default void error(MessageRequest request, Throwable throwable) {
    }
}
