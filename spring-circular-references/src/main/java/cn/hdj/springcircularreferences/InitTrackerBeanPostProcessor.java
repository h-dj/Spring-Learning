package cn.hdj.springcircularreferences;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class InitTrackerBeanPostProcessor implements BeanPostProcessor {


    /**
     * 在bean初始化之前执行
     *
     * @param bean
     * @param beanName
     * @return
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean.getClass().getPackageName().contains("cn.hdj")) {
            System.out.println(" 初始化完成：" + bean.getClass().getSimpleName() + "  " + bean.getClass().getName());
        }


        return bean;
    }
}
