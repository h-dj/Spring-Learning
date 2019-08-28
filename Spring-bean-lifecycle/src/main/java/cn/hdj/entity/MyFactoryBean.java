package cn.hdj.entity;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author hdj
 * @Description:
 * @date 8/26/19
 */
public class MyFactoryBean implements FactoryBean<Teacher> {

    public MyFactoryBean() {
        System.out.println("---FactoryBean MyFactoryBean----");
    }

    @Override
    public Teacher getObject() throws Exception {
        System.out.println("----FactoryBean.getObject-----");
        return new Teacher();
    }

    @Override
    public Class<?> getObjectType() {
        System.out.println("----FactoryBean.getObjectType-----");
        return Teacher.class;
    }
}
