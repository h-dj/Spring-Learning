package cn.hdj.entity;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author hdj
 * @Description:
 * @date 8/21/19
 */
public class Student implements InitializingBean, DisposableBean, BeanFactoryAware, BeanNameAware {

    private String name;
    private Integer age;


    public Student() {
        System.out.println("初始化构造函数");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @PostConstruct
    public void springPostConstruct() {
        System.out.println("---@PostConstruct--- 执行");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("---InitializingBean.afterPropertiesSet---");
    }

    public void myInitMethod() {
        System.out.println("---init-method---");
    }

    @Override
    public String toString() {
        return "Student{" +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("---BeanNameAware.setBeanName---");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("---BeanFactoryAware.setBeanFactory---");
    }


    @PreDestroy
    public void springPreDestroy(){
        System.out.println("-----@PreDestroy-----");

    }
    @Override
    public void destroy() throws Exception {
        System.out.println("-----DisposableBean.destroy()------");
    }

    public void myDestroyMethod(){
        System.out.println("---destroy-method---");
    }

}
