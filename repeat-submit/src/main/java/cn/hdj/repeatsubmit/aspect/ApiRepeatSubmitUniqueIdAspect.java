package cn.hdj.repeatsubmit.aspect;


import cn.hdj.repeatsubmit.po.DeduplicatePO;
import cn.hdj.repeatsubmit.service.IDeduplicateService;
import cn.hdj.repeatsubmit.utils.SpringElUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 使用 去重表 实现幂等
 * @author huangjiajin
 */
@Component
@Aspect
public class ApiRepeatSubmitUniqueIdAspect {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IDeduplicateService iDeduplicateService;


    @Pointcut("@annotation(cn.hdj.repeatsubmit.aspect.ApiRepeatUniqueIdSubmit)")
    public void pointCut() {
    }


    @Transactional(rollbackFor = Exception.class)
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Signature signature = joinPoint.getSignature();
        MethodSignature msig = (MethodSignature) signature;
        Method targetMethod = msig.getMethod();


        ApiRepeatUniqueIdSubmit apiRepeatSubmit = targetMethod.getAnnotation(ApiRepeatUniqueIdSubmit.class);
        String keyExpression = apiRepeatSubmit.keyExpression();

        Map<String, Object> argMap = SpringElUtil.getArgMap(joinPoint);

        String uniqueId = SpringElUtil.<String>createElBuilder()
                .setArgMap(argMap)
                .setBeanFactory(applicationContext)
                .setTarget(String.class)
                .setKeyExpression(keyExpression)
                .build();

        LambdaQueryWrapper<DeduplicatePO> queryWrapper = Wrappers.<DeduplicatePO>lambdaQuery()
                .eq(DeduplicatePO::getUniqueId, uniqueId);

        long count = this.iDeduplicateService.count(queryWrapper);
        if (count > 0) {
            throw new DuplicateKeyException("不要重复提交!");
        }

        //插入去重表
        DeduplicatePO deduplicatePO = new DeduplicatePO();
        deduplicatePO.setUniqueId(uniqueId);
        try {
            this.iDeduplicateService.save(deduplicatePO);
        } catch (Exception e) {
            throw new DuplicateKeyException("不要重复提交!");
        }

        Object proceed = joinPoint.proceed();

        //执行完删除
        this.iDeduplicateService.removeById(deduplicatePO);

        return proceed;
    }


}
