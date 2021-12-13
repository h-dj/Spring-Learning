package cn.hdj.repeatsubmit.aspect;


import java.lang.annotation.*;

/**
 * @Description:  幂等 使用锁的方式
 * @Author huangjiajian
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ApiRepeatLockSubmit {

    /**
     * 唯一key，支持Spring EL 表达式
     *
     * @return
     * @ 符号引用 Spring 注册的bean
     * # 符合引用方法上的参数
     * param?.id  其中? 是避免param为空时，发生空指针异常
     * @see <a>https://docs.spring.io/spring-framework/docs/3.0.x/reference/expressions.html</a>
     */
    String keyExpression();
}
