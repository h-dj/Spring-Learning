package cn.hdj.quartz;

import cn.hdj.quartz.controller.TestController;
import cn.hdj.quartz.job.HelloJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class QuartzApplication {


    public static void main(String[] args) throws SchedulerException {
        ConfigurableApplicationContext context = SpringApplication.run(QuartzApplication.class, args);
        Scheduler scheduler = context.getBean(Scheduler.class);
        scheduler.resumeAll();
    }

}
