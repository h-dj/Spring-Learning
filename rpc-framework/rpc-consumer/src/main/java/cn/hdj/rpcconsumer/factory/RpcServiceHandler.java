package cn.hdj.rpcconsumer.factory;


import cn.hdj.rpcconsumer.RpcRequest;
import cn.hdj.rpcconsumer.RpcResponse;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.Socket;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午2:41
 * @description: 客户端代理服务，客户端往服务端发起的调用将通过客户端代理来发起
 */
public class RpcServiceHandler implements InvocationHandler {
    private String host;    // 服务端地址
    private int port;        // 服务端口号

    public RpcServiceHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 动态代理做的事情，接口的实现不在本地，在网络中的其他进程中，我们通过实现了Rpc客户端的对象来发起远程服务的调用。
     */
    @Override
    public Object invoke(Object obj, Method method, Object[] params) throws Throwable {
        // 调用前
        System.out.println("执行远程方法前，可以做些事情");
        // 封装参数，类似于序列化的过程
        RpcRequest request = new RpcRequest();
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParamTypes(method.getParameterTypes());
        request.setParams(params);
        // 链接服务器调用服务
        Object rst = execute(request,method);

        // 调用后
        System.out.println("执行远程方法后，也可以做些事情");
        return rst;
    }

    private Object execute(RpcRequest request,Method method) throws Throwable {
        // 打开远端服务连接
        Socket server = new Socket(host, port);
        OutputStream out = null;
        InputStream in = null;
        try {
            // 1. 服务端输出流，写入请求数据，发送请求数据
            out = server.getOutputStream();
            IoUtil.writeUtf8(out, false, JSONUtil.toJsonStr(request));
            IoUtil.flush(out);
            server.shutdownOutput();

            // 2. 服务端输入流，获取返回数据，转换参数类型
            // 类似于反序列化的过程
            in = server.getInputStream();
            String json = IoUtil.read(in, "utf-8");
            RpcResponse response = JSONUtil.toBean(json, new TypeReference<RpcResponse>() {
                @Override
                public Type getType() {
                    return TypeUtil.getReturnType(method);
                }
            }, true);
            // 3. 返回服务端响应结果
            if (response.getError() != null) { // 服务器产生异常
                throw response.getError();
            }
            return response.getResult();
        } finally {
            IoUtil.close(in);
            IoUtil.close(out);
            IoUtil.close(server);
        }
    }
}
