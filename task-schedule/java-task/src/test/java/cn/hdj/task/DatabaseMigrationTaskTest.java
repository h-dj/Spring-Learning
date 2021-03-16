package cn.hdj.task;

import cn.hdj.task.jop.DatabaseMigrationTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/3/4 16:16
 */
public class DatabaseMigrationTaskTest {



    /**
     * 指定时间执行 任务
     *
     * @throws Exception
     */
    @Test
    public void givenDatabaseMigrationTask_whenTimerScheduledForNowPlusTwoSeconds_thenDataMigratedAfterTwoSeconds() throws Exception {
        //定义两个数据库
        List<String> oldDatabase = Arrays.asList("Harrison Ford", "Carrie Fisher", "Mark Hamill");
        List<String> newDatabase = new ArrayList<>();


        LocalDateTime twoSecondsLater = LocalDateTime.now().plusSeconds(2);
        Date twoSecondsLaterAsDate = Date.from(twoSecondsLater.atZone(ZoneId.systemDefault()).toInstant());

        //指定两秒后执行任务
        new Timer().schedule(new DatabaseMigrationTask(oldDatabase, newDatabase), twoSecondsLaterAsDate);

        while (LocalDateTime.now().isBefore(twoSecondsLater)) {
            //时间没有到两秒，新数据库是空的，因为还没执行任务
            Assertions.assertTrue(newDatabase.isEmpty());
            Thread.sleep(500);
        }

        Assertions.assertTrue(newDatabase.size() == oldDatabase.size());
    }

    /**
     * 延迟2秒后执行任务
     *
     * @throws Exception
     */
    @Test
    public void givenDatabaseMigrationTask_whenTimerScheduledInTwoSeconds_thenDataMigratedAfterTwoSeconds() throws Exception {
        List<String> oldDatabase = Arrays.asList("Harrison Ford", "Carrie Fisher", "Mark Hamill");
        List<String> newDatabase = new ArrayList<>();

        new Timer().schedule(new DatabaseMigrationTask(oldDatabase, newDatabase), 2000);

        LocalDateTime twoSecondsLater = LocalDateTime.now().plusSeconds(2);

        while (LocalDateTime.now().isBefore(twoSecondsLater)) {
            Assertions.assertTrue(newDatabase.isEmpty());
            Thread.sleep(500);
        }
        Assertions.assertTrue(newDatabase.size() == oldDatabase.size());
    }

}
