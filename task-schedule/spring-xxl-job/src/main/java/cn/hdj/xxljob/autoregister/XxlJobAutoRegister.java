package cn.hdj.xxljob.autoregister;

import cn.hdj.xxljob.autoregister.service.JobGroupService;
import cn.hdj.xxljob.autoregister.service.JobInfoService;
import cn.hdj.xxljob.autoregister.service.JobLoginService;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import cn.hdj.xxljob.config.XxlJobProperties;

import java.io.IOException;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/11/23 17:47
 */
@Slf4j
@Component
public class XxlJobAutoRegister implements ApplicationContextAware, ApplicationListener<ApplicationReadyEvent> {

    private ApplicationContext applicationContext;
    @Autowired
    private XxlJobProperties xxlJobProperties;

    @Autowired
    private JobGroupService jobGroupService;

    @Autowired
    private JobLoginService jobLoginService;

    @Autowired
    private JobInfoService jobInfoService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        try {
            //自动注册执行器
            autoRegisterJobGroup();

            //自动注册任务
            autoRegisterJob();
        } catch (IOException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    private void autoRegisterJob() throws IOException {
        log.info("=============== 自动注册job ==============");
        this.jobLoginService.login();
        this.jobInfoService.autoRegister();
    }

    private void autoRegisterJobGroup() throws IOException {
        if (StrUtil.isEmpty(xxlJobProperties.getExecutor().getAppname())) {
            log.warn("=============== 自动注册执行器  失败==============  appname isEmpty");
        } else {
            log.info("=============== 自动注册执行器 ==============" + xxlJobProperties.getExecutor().getAppname());
            jobLoginService.login();
            jobGroupService.autoRegister();
        }

    }
}
