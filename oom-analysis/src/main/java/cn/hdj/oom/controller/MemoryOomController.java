package cn.hdj.oom.controller;

import cn.hdj.oom.domain.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author huangjiajian
 * @Date 2022/8/14 下午3:07
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@RestController
@RequestMapping(value = "/api/memory")
public class MemoryOomController {
    private List<UserDTO> users = new ArrayList<>();

    @GetMapping("/clear")
    public void clear() {
        this.users.clear();
    }


    /**
     * 堆内存溢出
     * <pre>
     *   为了更快的看到效果，限制最大和最小内存：
     *   -Xmx32M -Xms32M
     *
     *   OOM 触发导出 dump 文件
     *   -XX:+HeapDumpOnOutOfMemoryError
     *   -XX:HeapDumpPath=/home/h-dj/oom
     *
     *    日志
     *   -XX:+PrintGCDateStamps
     *  -XX:+PrintTenuringDistribution
     *  -XX:+PrintGCApplicationConcurrentTime
     *  -XX:+PrintGCDetails
     *  -Xloggc:/home/h-dj/logs/oom-log/oom-analysis.log
     *  -XX:+UseGCLogFileRotation
     *  -XX:NumberOfGCLogFiles=10
     *  -XX:GCLogFileSize=20M
     *
     *
     *   记得需要在启动的时候添加启动参数
     * </pre>
     */
    @GetMapping("/heap")
    public void heap() throws InterruptedException {
        long i = 0;
        while (true) {
            users.add(new UserDTO(i++, UUID.randomUUID().toString()));
        }
    }


    /**
     * 测试死循环
     *
     * @throws InterruptedException
     */
    @GetMapping("/loop")
    public void loop() throws InterruptedException {
        while (true) {
            continue;
        }
    }


    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    /**
     * 死锁
     *
     * 死锁条件
     *  互斥条件：一个资源每次只能被一个线程使用。
     * • 保持和请求条件：一个线程因请求资源而阻塞时，对已获得资源保持不放。
     * • 不可剥夺调教：线程已获得资源，在未使用完成前，不能被剥夺。
     * • 循环等待条件：若干线程之间形成一种头尾相接的循环等待资源关系。
     *
     */
    @RequestMapping("/deadlock")
    public String deadlock() {
        new Thread(() -> {
            synchronized (lock1) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                synchronized (lock2) {
                    System.out.println("Thread1 over");
                }
            }
        }).start();
        new Thread(() -> {
            synchronized (lock2) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                synchronized (lock1) {
                    System.out.println("Thread2 over");
                }
            }
        }).start();
        return "deadlock";
    }
}
