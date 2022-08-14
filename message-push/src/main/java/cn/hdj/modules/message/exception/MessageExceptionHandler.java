package cn.hdj.modules.message.exception;

import cn.hdj.modules.api.domain.MessageSendRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author huangjiajian
 * @Date 2022/8/7 下午3:57
 * @Description: 异常处理
 */
@Slf4j
@Component
public class MessageExceptionHandler {

    public void handler(MessageSendRequest request, Exception e) {
        log.error(e.getMessage(), e);
    }
}
