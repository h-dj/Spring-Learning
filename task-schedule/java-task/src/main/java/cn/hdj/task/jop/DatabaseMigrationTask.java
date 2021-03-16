package cn.hdj.task.jop;

import java.util.List;
import java.util.TimerTask;

/**
 * @Description: 任务, 数据库合并
 * @Author huangjiajian
 * @Date 2021/3/4 16:20
 */
public class DatabaseMigrationTask extends TimerTask {
    private List<String> oldDatabase;
    private List<String> newDatabase;

    public DatabaseMigrationTask(List<String> oldDatabase, List<String> newDatabase) {
        this.oldDatabase = oldDatabase;
        this.newDatabase = newDatabase;
    }

    @Override
    public void run() {
        newDatabase.addAll(oldDatabase);
        System.out.printf("完成数据库合并");
    }
}
