package cn.hdj.rpcconsumer.factory;

import java.lang.reflect.Proxy;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午5:09
 * @description: rpc服务工厂
 */
public class RpcServiceFactory {


    public static <T> T create(Class<T> clazz, String host, int port) {
        RpcServiceHandler rpcServiceHandler = new RpcServiceHandler(host, port);
        // clazz 不是接口不能使用JDK动态代理
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, rpcServiceHandler);
    }
}
