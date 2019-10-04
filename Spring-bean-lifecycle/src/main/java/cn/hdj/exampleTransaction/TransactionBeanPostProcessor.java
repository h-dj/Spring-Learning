package cn.hdj.exampleTransaction;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author hdj
 * @version 1.0
 * @date 2019/10/3 12:18
 * @description:
 */
public class TransactionBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //这里返回代理类
        System.out.println("Bean '" + beanName + "' created : " + bean.toString());
        return bean;
    }
}
