package cn.hdj.fastboot.common.exception;

import lombok.Getter;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/4/15 14:31
 */
@Getter
public class BaseException extends RuntimeException {
    /**
     * 业务状态码
     */
    private Integer code;

    public BaseException(String message, Integer code) {
        this(message,code,null);
    }

    public BaseException(String message, Integer code, Throwable throwable) {
        this(message,throwable,false,true,code);
    }

    public BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Integer code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
