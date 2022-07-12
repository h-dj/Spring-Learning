package cn.hdj.server.exception;

import lombok.Getter;

/**
 * @Author huangjiajian
 * @Date 2022/7/12 下午11:33
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Getter
public class ApiException extends RuntimeException {

    /**
     * 业务状态码
     */
    private Integer code;

    public ApiException(String message, Integer code) {
        this(message, code, null);
    }

    public ApiException(String message, Integer code, Throwable throwable) {
        this(message, throwable, false, true, code);
    }

    public ApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Integer code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
