package cn.hdj.springbootaopdemo;

import cn.hdj.springbootaopdemo.proxy.jdk.Car;
import cn.hdj.springbootaopdemo.proxy.jdk.JDKProxyFactory;
import cn.hdj.springbootaopdemo.proxy.jdk.Taxi;
import org.junit.Test;

public class JdkProxyTest {


    @Test
    public void test_jdk_proxy(){
        Car car=new Taxi();
        JDKProxyFactory jdkProxyFactory=new JDKProxyFactory(car);
        Car proxy = (Car) jdkProxyFactory.createProxy();
        proxy.running();
    }

}
