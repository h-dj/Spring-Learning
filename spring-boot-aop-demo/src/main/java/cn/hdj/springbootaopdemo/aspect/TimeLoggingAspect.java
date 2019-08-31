package cn.hdj.springbootaopdemo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author hdj
 * @Description: 这是打印执行时间的日志切面
 * @date 8/30/19
 */
@Aspect  //切面注解
@Component
public class TimeLoggingAspect {

    @Pointcut("execution(* cn.hdj.springbootaopdemo.service.UserService.*(..))")
    public void userPointCut() {
    }

    @Pointcut("execution(* cn.hdj.springbootaopdemo.service.UserService.save*(..))")
    public void savePointCut() {
    }

    /**
     * 定义切入点
     * 1. 应用通知在包cn.hdj.springbootaopdemo.service下所有的类
     * > @Pointcut("execution(* cn.hdj.springbootaopdemo.service.*.*(..))")
     * <p>
     * 2. 应用通知在某个类下的某个方法
     * > @Pointcut("execution(* cn.hdj.springbootaopdemo.service.UserService.updateUser(..))")
     * 3. 应用通知到某个类下的所有方法
     * > @Pointcut("execution(* cn.hdj.springbootaopdemo.service.UserService.*(..))")
     * <p>
     * 4. 匹配前缀为list 的方法
     * > @Pointcut(value = "execution(*  cn.hdj.springbootaopdemo.service.UserService.list*(..))")
     * <p>
     * 5. 某个包下所有的方法
     * > @Pointcut(value = "execution(*  cn.hdj.springbootaopdemo.service.*.*(..))")
     */
    @Pointcut(value = "bean(user)")
    public void pointCut() {
    }






    /**
     * 后置异常通知
     */
    @AfterThrowing(pointcut = "pointCut()", throwing = "exception")
    public void afterThrowingAdvice(Exception exception) {
        System.out.println("=====后置异常通知=====" + exception);
    }

    /**
     * 后置返回通知(抛出异常，不会执行)
     */
    @AfterReturning("pointCut()")
    public void afterReturningAdvice() {
        System.out.println("=====后置返回通知=====");
    }


    /**
     * 环绕通知
     *
     * @param joinPoint 连接点对象
     */
    @Around("pointCut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("=====环绕通知=======开始, :" + joinPoint.getSignature().getName());
        Object proceed = joinPoint.proceed();
        System.out.println("=====环绕通知=======结束 :" + joinPoint.getSignature().getName());
        return proceed;
    }

    /**
     * 后置通知
     */
    @After("pointCut()")
    public void afterAdvice() {
        System.out.println("=====后置通知=====");
    }

    /**
     * 前置通知
     */
    @Before("pointCut()")
    public void beforeAdvice() {
        System.out.println("=====前置通知======");
    }
}
