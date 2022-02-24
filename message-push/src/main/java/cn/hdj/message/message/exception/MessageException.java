package cn.hdj.message.message.exception;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2022/2/23 17:39
 */
public class MessageException extends RuntimeException{

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
