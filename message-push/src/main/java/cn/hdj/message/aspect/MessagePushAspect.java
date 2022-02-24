package cn.hdj.message.aspect;

import cn.hdj.message.aspect.annotation.MessageSend;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.reflect.Method;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/2/24 14:12
 */
@Aspect
public class MessagePushAspect {

    @Pointcut("@annotation(cn.hdj.message.aspect.annotation.MessageSend)")
    public void pointcut() {
    }


    @Around(value = "pointcut()")
    public Object send(ProceedingJoinPoint point) throws Throwable {

        //获取方法
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        //注解
        MessageSend annotation = method.getAnnotation(MessageSend.class);

        //参数
        Object[] args = point.getArgs();


        Object proceed = point.proceed();

        return proceed;

    }


}
