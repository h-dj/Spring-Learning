package cn.hdj.rpcconsumer;

import cn.hdj.rpcconsumer.factory.RpcServiceFactory;
import cn.hdj.rpcconsumer.service.StudentService;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午2:38
 * @description:
 */
public class RpcConsumerApp {


    public static void main(String[] args) {
        StudentService studentService = RpcServiceFactory.create(StudentService.class, "127.0.0.1", 9999);
        System.out.println(studentService.getInfo());
    }
}
