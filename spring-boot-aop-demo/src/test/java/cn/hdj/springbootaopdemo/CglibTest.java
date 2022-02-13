package cn.hdj.springbootaopdemo;

import cn.hdj.springbootaopdemo.proxy.cglib.CglibProxyFactory;
import cn.hdj.springbootaopdemo.proxy.jdk.Car;
import cn.hdj.springbootaopdemo.proxy.jdk.Taxi;
import org.junit.Test;
import org.springframework.cglib.core.DebuggingClassWriter;

public class CglibTest {

    @Test
    public void test_cglib(){
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "/home/hdj/IdeaProjects/Spring-Learning/spring-boot-aop-demo/target");
        Car car=new Taxi();
        CglibProxyFactory factory=new CglibProxyFactory(car);
        Car proxy = (Car) factory.createProxy();
        proxy.running();
    }
}
