package cn.hdj;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * @author hdj
 * @Description: Bean 预处理器
 * @date 8/22/19
 */
public class MyBeanPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        System.out.println("BeanPostProcessor.postProcessAfterInitialization");
        return bean;
    }
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        System.out.println("BeanPostProcessor.postProcessBeforeInitialization");
        return bean;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
