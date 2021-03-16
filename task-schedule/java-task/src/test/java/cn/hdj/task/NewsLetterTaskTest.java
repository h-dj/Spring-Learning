package cn.hdj.task;

import cn.hdj.task.jop.NewsletterTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/3/4 17:22
 */
public class NewsLetterTaskTest {

    private final Timer timer = new Timer();

    @AfterEach
    void afterEach() {
        timer.cancel();
    }


    /**
     * 使用Timer调度执行任务，有两种模式： fixed delay（固定延迟）和 fixed rate （固定频率）
     *
     * fixed delay（固定延迟）： 固定执行间隔 ,单位：毫秒。 当前时间 + 任务执行间隔时间  注意，以调用完成时刻为开始计时时间。
     *
     * fixed rate （固定频率）： 固定执行间隔 ,单位：毫秒。 下一次执行时间 + 任务执行间隔时间  以调用开始时刻为开始计时时间
     *
     *
     * <p>
     * FixeDelay
     * <pre>
     * 0s     1s    2s     3s           5s
     * |--T1--|
     * |-----2s-----|--1s--|-----T2-----|
     * |-----2s-----|--1s--|-----2s-----|--T3--|
     * </pre>
     *
     * @throws InterruptedException
     */
    @Test
    void givenNewsletterTask_whenTimerScheduledEachSecondFixedDelay_thenNewsletterSentEachSecond() throws Exception {
        timer.schedule(new NewsletterTask(), 0, 1000);

        for (int i = 0; i < 3; i++) {
            Thread.sleep(1000);
        }
    }

    /**
     *FixeRate
     * <pre>
     * 0s     1s    2s     3s    4s
     * |--T1--|
     * |-----2s-----|--1s--|-----T2-----|
     * |-----2s-----|-----2s-----|--T3--|
     * </pre>
     * @throws Exception
     */
    @Test
    void givenNewsletterTask_whenTimerScheduledEachSecondFixedRate_thenNewsletterSentEachSecond() throws Exception {
        timer.scheduleAtFixedRate(new NewsletterTask(), 0, 1000);

        for (int i = 0; i < 3; i++) {
            Thread.sleep(1000);
        }
    }


}
