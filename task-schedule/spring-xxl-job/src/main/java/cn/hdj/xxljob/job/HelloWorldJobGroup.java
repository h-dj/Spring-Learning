package cn.hdj.xxljob.job;

import cn.hdj.xxljob.autoregister.JobAutoRegisterMarker;
import cn.hdj.xxljob.autoregister.annotation.XxlRegister;
import cn.hutool.core.date.DateUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/4/28 18:01
 */
@Component
public class HelloWorldJobGroup implements JobAutoRegisterMarker {


    /**
     * 一个方法就是一个执行器
     *
     */
    @XxlJob("testJob")
    @XxlRegister(cron = "0 0 0 * * ? *",
            author = "huangjiajian",
            jobDesc = "测试job"
    )
    public void demoJobHandler() {
        System.out.println("测试 Job"+ DateUtil.now());
    }
}
