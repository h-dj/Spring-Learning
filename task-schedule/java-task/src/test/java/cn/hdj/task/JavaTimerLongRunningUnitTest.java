package cn.hdj.task;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/3/4 17:42
 */
public class JavaTimerLongRunningUnitTest {


    /**
     * 创建两个任务，调度执行，不管添加任务的顺序
     */
    @Test
    public void givenUsingTimer_whenSchedulingDifferentDelayTaskTowWithoutSort_thenCorrect() throws InterruptedException {
        TimerTask taskA = new TimerTask() {
            public void run() {
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledExecutionTime()), ZoneId.systemDefault());
                System.out.println("TaskA  performed on: " + time +
                        " Thread's name: " + Thread.currentThread().getName());
            }
        };
        TimerTask taskB = new TimerTask() {
            public void run() {
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledExecutionTime()), ZoneId.systemDefault());
                System.out.println("TaskB performed on: " + time +
                        " Thread's name: " + Thread.currentThread().getName());
            }
        };

        //创建Timer
        Timer timer = new Timer("Timer");
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
        System.out.println(time);
        // 使用delay，延迟执行


        //taskB延迟 1 秒执行
        timer.schedule(taskB, 1000);

        //taskA延迟 0.5 秒执行
        timer.schedule(taskA, 500);


        TimeUnit.MINUTES.sleep(5);
    }



    /**
     * 可重复执行task
     *
     * @throws InterruptedException
     */
    @Test
    public void givenUsingTimer_whenSchedulingDailyTask_thenCorrect() throws InterruptedException {
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task performed on: " + new Date() + "\n" +
                        "Thread's name: " + Thread.currentThread().getName());
            }
        };
        Timer timer = new Timer("Timer");
        //延迟执行1秒
        long delay = 1000L;
        //一天后重复执行
        long period = 1000L * 60L * 60L * 24L;
        timer.schedule(task, delay, period);

        //等待Timer运行完成
        TimeUnit.SECONDS.sleep(2);
    }


    /**
     * 取消 TimerTask 任务
     *
     * @throws InterruptedException
     */
    @Test
    public void givenUsingTimer_whenCancelingTimerTask_thenCorrect() throws InterruptedException {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.printf("Task performed on " + new Date());
                this.cancel();
            }
        };
        final Timer timer = new Timer("Timer");

        final long delay = 1000L;
        final long period = 1000L;
        timer.scheduleAtFixedRate(task, delay, period);

        Thread.sleep(delay * 3);
    }

    @Test
    public void givenUsingTimer_whenCancelingTimer_thenCorrect()
            throws InterruptedException {
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task performed on " + new Date());
            }
        };

        TimerTask task2 = new TimerTask() {
            public void run() {
                System.out.println("Task2 performed on " + new Date());
            }
        };
        Timer timer = new Timer("Timer");

        timer.scheduleAtFixedRate(task, 1000L, 1000L);
        timer.scheduleAtFixedRate(task2, 0, 2000L);


        Thread.sleep(1000L * 2);
        timer.cancel();
    }


    @Test
    public void givenUsingTimer_whenStoppingThread_thenTimerTaskIsCancelled()
            throws InterruptedException {
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task performed on " + new Date());
                // TODO: stop the thread here
                //这里为了方便直接使用 stop停止线程 , 这个不推荐使用
                //因为Timer 使用单一线程去执行任务，线程被终止，则所有待执行的任务将会丢失
                Thread.currentThread().stop();
            }
        };
        Timer timer = new Timer("Timer");

        timer.scheduleAtFixedRate(task, 1000L, 1000L);

        Thread.sleep(1000L * 2);
    }


    @Test
    public void givenUsingExecutorService_whenSchedulingRepeatedTask_thenCorrect()
            throws InterruptedException {
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                System.out.println("Task performed on " + new Date());
            }
        };
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        long delay = 1000L;
        long period = 1000L;
        executor.scheduleAtFixedRate(repeatedTask, delay, period, TimeUnit.MILLISECONDS);
        Thread.sleep(delay + period * 3);
        executor.shutdown();
    }
}
