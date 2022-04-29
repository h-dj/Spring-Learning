package cn.hdj.message.message.exception;

import cn.hdj.message.message.enums.ErrorCodeEnum;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/2/25 13:46
 */
public class DeduplicateMessageException extends MessageException {


    public DeduplicateMessageException(int code, String message) {
        super(code, message);
    }

    public DeduplicateMessageException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum);
    }
}
