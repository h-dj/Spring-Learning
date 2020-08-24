package cn.hdj.provider;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午2:37
 * @description:
 */
@Data
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    // 可能抛出的异常
    private Throwable error;
    // 响应的内容或结果
    private Object result;
}
