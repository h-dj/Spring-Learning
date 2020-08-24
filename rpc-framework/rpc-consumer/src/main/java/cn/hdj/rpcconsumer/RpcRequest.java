package cn.hdj.rpcconsumer;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午2:36
 * @description: 包装需要调用服务端的方法参数
 */
@Data
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    // 需要请求的类名
    private String className;

    // 需求请求的方法名
    private String methodName;

    // 请求方法的参数类型
    private Class<?>[] paramTypes;

    // 请求的参数值
    private Object[] params;
}
