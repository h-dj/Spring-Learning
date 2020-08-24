package cn.hdj.provider;

import cn.hdj.provider.service.StudentService;
import cn.hdj.provider.service.StudentServiceImpl;

import java.io.IOException;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午2:30
 * @description:
 */
public class RpcProviderApp {

    public static void main(String[] args) throws IOException {
        //实例化服务
        StudentService studentService = new StudentServiceImpl();
        //暴露服务
        RpcProvider.provider(studentService, 9999);
    }

}
