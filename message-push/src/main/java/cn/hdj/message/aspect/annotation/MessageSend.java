package cn.hdj.message.aspect.annotation;

import java.lang.annotation.*;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/2/24 14:14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MessageSend {

    /**
     * 模板ID
     *
     * @return
     */
    String templateId();

    /**
     * 参数业务编码
     *
     * @return
     */
    String messageTypeCode();

    /**
     * 是否执行完后发送
     *
     * @return
     */
    boolean after() default false;

}
