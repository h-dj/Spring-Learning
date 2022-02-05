package cn.hdj.repeatsubmit.aspect;


import cn.hdj.repeatsubmit.utils.SpringElUtil;
import cn.hdj.repeatsubmit.utils.ZookeeperLockUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonRedLock;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 使用 去重表 实现幂等
 *
 * @author huangjiajin
 */
@Component
@Aspect
public class ApiRepeatSubmitLockAspect {

    public static final String LOCK_PREFIX = "lock:";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedissonClient redissonClient;


    private CuratorFramework curatorFramework;

//    @Autowired
    public void setCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        ZookeeperLockUtil.setCuratorFramework(curatorFramework);
    }

    @Pointcut("@annotation(cn.hdj.repeatsubmit.aspect.ApiRepeatLockSubmit)")
    public void pointCut() {
    }

    /**
     * 分布式式锁使用 Redisson
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
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

        RLock lock = this.redissonClient.getLock(LOCK_PREFIX + uniqueId);

        //判断是否被抢占了锁
        if (lock.isLocked()) {
            throw new DuplicateKeyException("不要重复提交!");
        }

        //尝试获取锁， 默认30秒会超时过期， 并启动线程监听，自动续签
        //当客户端异常，终止了续签线程，超时后会删除锁，避免发生死锁
        //如果自己手动设置了超时过期时间，则不会启动线程监听，自动续签
        if (lock.tryLock(1,TimeUnit.DAYS)) {
            try {
                return joinPoint.proceed();
            } finally {
                //释放锁
                lock.unlock();
            }
        }
        throw new DuplicateKeyException("不要重复提交!");
    }


    /**
     * 分布式式锁使用 Zookeeper
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
//    @Around("pointCut()")
    public Object around2(ProceedingJoinPoint joinPoint) throws Throwable {

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

        //尝试获取锁，不等待，没有获取到马上抛出异常
        //Zookeeper lock 采用的是临时节点，客户端会话失效或连接关闭后，该节点会被自动删除，避免发生死锁
        InterProcessMutex interProcessMutex = ZookeeperLockUtil.tryLock(uniqueId, 0, TimeUnit.SECONDS);
        if (interProcessMutex != null) {
            try {
                return joinPoint.proceed();
            } finally {
                //释放锁
                ZookeeperLockUtil.unLock(uniqueId, interProcessMutex);
            }
        }
        throw new DuplicateKeyException("不要重复提交!");
    }
}
