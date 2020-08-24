package cn.hdj.provider;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午3:03
 * @description:
 */
public class RpcProvider {

    /**
     * 线程池
     */
    private static final ExecutorService executorService = new ThreadPoolExecutor(
            10,
            10,
            3,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>()
    );

    /**
     * rpc 提供端，暴露服务
     */
    public static <T> void provider(final T service, int port) throws IOException {
        //创建服务端的套接字，绑定端口port
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            //开始接收客户端的消息，并以此创建套接字
            final Socket socket = serverSocket.accept();
            executorService.execute(new Handler(socket, service));
        }
    }


    /**
     * 响应调用端
     *
     * @param oout
     * @param result
     */
    private static void response(OutputStream oout, Object result, Throwable throwable) {
        RpcResponse response = new RpcResponse();
        response.setResult(result);
        response.setError(throwable);
        IoUtil.writeUtf8(oout, false, JSONUtil.toJsonStr(response));
        IoUtil.flush(oout);
        System.out.println(Thread.currentThread().getName() + "=====> 响应结果" + response);
    }


    public static class Handler<T> implements Runnable {

        private final Socket socket;
        private T service;

        public Handler(Socket socket, T service) {
            this.socket = socket;
            this.service = service;
        }


        @Override
        public void run() {
            final Socket client = socket;
            //创建呢一个对内传输的对象流，并绑定套接字
            RpcRequest request = null;
            InputStream in;
            try {
                // 1. 获取流以待操作
                in = client.getInputStream();
                String json = IoUtil.read(in, "utf-8");
                System.out.println(Thread.currentThread().getName() + "<===== 接受rpc 调用端请求" + json);
                //2读取参数
                request = JSONUtil.toBean(json, RpcRequest.class);

                // 3. 执行服务方法, 返回结果
                Class<?> clazz = service.getClass();
                Method method = clazz.getMethod(request.getMethodName(), request.getParamTypes());
                Object result = method.invoke(service, request.getParams());
                System.out.println(Thread.currentThread().getName() + "<===== 处理结果" + result);
                // 4. 返回RPC响应，序列化RpcResponse
                response(client.getOutputStream(), result, null);

            } catch (Exception e) {
                try {    //异常处理
                    if (client.getOutputStream() != null) {
                        response(client.getOutputStream(), null, e);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } finally {
                IoUtil.close(client);
            }
        }
    }
}
