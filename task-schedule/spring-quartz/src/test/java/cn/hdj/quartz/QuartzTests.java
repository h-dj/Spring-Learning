package cn.hdj.quartz;

import cn.hdj.quartz.job.HelloJob;
import cn.hdj.quartz.job.SimpleJob;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/4/26 14:38
 */
public class QuartzTests extends QuartzApplicationTests{


    @Autowired
    private  Scheduler scheduler;

    @Test
    public void test_scheduler_create(){
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();

            scheduler.shutdown();

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }



    @Test
    public void test_scheduler_start() throws SchedulerException, InterruptedException {
        //创建一个scheduler

        scheduler.getContext().put("skey", "svalue");

        //创建一个Trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .usingJobData("t1", "tv1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3)
                        .repeatForever()).build();
        trigger.getJobDataMap().put("t2", "tv2");

        //创建一个job
        JobDetail job = JobBuilder.newJob(HelloJob.class)
                .usingJobData("j1", "jv1")
                .withIdentity("myjob", "mygroup").build();
        job.getJobDataMap().put("j2", "jv2");

        //注册trigger并启动scheduler
        scheduler.scheduleJob(job,trigger);
        scheduler.start();

        TimeUnit.HOURS.sleep(10);
    }

    @Test
    public void test_create_job_dynamic() throws SchedulerException, InterruptedException {

        String jobName = "测试一";
        String jobGroup = "测试组";

        String triggerName = "测试一";
        String triggerGroup = "测试组";

        JobKey jobKey = new JobKey(jobName,jobGroup);

        // 如果存在这个任务，则删除
        if(scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        /**
         * 创建任务
         */
        JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                .withIdentity(jobKey)
                .build();

        /**
         * 表达式
         */
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0/1 * * * * ?");

        /**
         * 触发器
         */
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName,triggerGroup)
                .withSchedule(cronScheduleBuilder).build();
        /**
         * 持久化到数据库，启动
         */
        scheduler.scheduleJob(jobDetail,trigger);

        TimeUnit.HOURS.sleep(10);
    }

    @Test
    public void test_pause_task() throws SchedulerException {
        String jobName = "测试一";
        String jobGroup = "测试组";

        JobKey jobKey = new JobKey(jobName,jobGroup);
        this.scheduler.pauseJob(jobKey);
    }
}
