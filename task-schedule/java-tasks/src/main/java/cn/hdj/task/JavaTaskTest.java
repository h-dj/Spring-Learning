package cn.hdj.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/3/17 16:26
 */
public class JavaTaskTest {

    public static void main(String[] args) {
        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println(Thread.currentThread().getName() + " ---> task1 " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 0, 1000);

        // task2 会在task14执行完后，才会执行
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println(Thread.currentThread().getName() + " ---> task2 " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//            }
//        }, 0, 1000);

        //固定速率
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " ---> task3 " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                System.out.println(Thread.currentThread().getName() + " ---> task4 " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        }, 0, 1000);
    }
}
