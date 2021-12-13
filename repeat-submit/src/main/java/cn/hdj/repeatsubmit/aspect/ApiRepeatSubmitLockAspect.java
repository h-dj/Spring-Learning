package cn.hdj.repeatsubmit.aspect;


import cn.hdj.repeatsubmit.po.DeduplicatePO;
import cn.hdj.repeatsubmit.service.IDeduplicateService;
import cn.hdj.repeatsubmit.utils.SpringElUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用 去重表 实现幂等
 *
 * @author huangjiajin
 */
@Component
@Aspect
public class ApiRepeatSubmitLockAspect {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedissonClient redissonClient;


    @Pointcut("@annotation(cn.hdj.repeatsubmit.aspect.ApiRepeatLockSubmit)")
    public void pointCut() {
    }


    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Signature signature = joinPoint.getSignature();
        MethodSignature msig = (MethodSignature) signature;
        Method targetMethod = msig.getMethod();


        ApiRepeatLockSubmit apiRepeatSubmit = targetMethod.getAnnotation(ApiRepeatLockSubmit.class);
        String keyExpression = apiRepeatSubmit.keyExpression();

        Map<String, Object> argMap = SpringElUtil.getArgMap(joinPoint);

        String uniqueId = SpringElUtil.<String>createElBuilder()
                .setArgMap(argMap)
                .setBeanFactory(applicationContext)
                .setTarget(String.class)
                .setKeyExpression(keyExpression)
                .build();


        return joinPoint.proceed();
    }
}
