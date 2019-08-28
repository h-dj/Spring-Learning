package cn.hdj.exampleTransaction;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author hdj
 * @Description:
 * @date 8/26/19
 */
public class ShiroFilterFactoryBean implements FactoryBean, BeanPostProcessor {


    @Override
    public Object getObject() throws Exception {
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }



}
