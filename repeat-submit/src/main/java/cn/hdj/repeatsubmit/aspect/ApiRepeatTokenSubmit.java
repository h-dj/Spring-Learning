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
public @interface ApiRepeatTokenSubmit {

}
