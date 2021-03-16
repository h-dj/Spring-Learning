package cn.hdj.springtask.task;

import javafx.scene.paint.Stop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjiajian
 * <p>
 * 创建定时任务
 */
@Slf4j
@Component
public class ScheduledTasks {


    /**
     * 5s 重复执行, 使用 fixedRate、fixedDelay、cron
     */
//    @Scheduled(fixedRate = 5000)
//    @Scheduled(cron = "*/5 * * * * ?")
//    @Scheduled(fixedDelay = 5000)
    public void reportCurrentTime() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),ZoneId.systemDefault());
        log.info("The time is now {}", dateTime);
    }


    /**
     * 不同任务间是否会影彼此 (taskA 和 taskB 是串行执行)
     * 设置调度线程池  或者  设置异步线程池 + @Async
     */
    @Scheduled(cron = "*/5 * * * * ?")
    @Async
    public void taskA() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),ZoneId.systemDefault());
        log.info("taskA  处理任务 {}", dateTime);
    }


    @Scheduled(cron = "*/1 * * * * ?")
    @Async
    public void taskB() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),ZoneId.systemDefault());
        log.info("taskB  处理任务 {}", dateTime);
    }



}