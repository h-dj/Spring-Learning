package cn.hdj.repeatsubmit.aspect;


import cn.hdj.repeatsubmit.service.TokenService;
import cn.hdj.repeatsubmit.utils.SpringElUtil;
import cn.hdj.repeatsubmit.utils.ZookeeperLockUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 使用 去重表 实现幂等
 *
 * @author huangjiajin
 */
@Component
@Aspect
public class ApiRepeatSubmitTokenAspect {


    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HttpServletRequest request;


    @Pointcut("@annotation(cn.hdj.repeatsubmit.aspect.ApiRepeatTokenSubmit)")
    public void pointCut() {
    }



    @Before("pointCut()")
    public void Before(JoinPoint joinPoint) {
        tokenService.checkToken(request);
    }


}
