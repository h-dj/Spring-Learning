package cn.hdj.springcircularreferences;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hdj
 * @date 2023/10/17 16:23
 * @description
 */
@Service
public class ServiceD {

    @Autowired
    private ServiceC serviceC;

    public void doSomething() {
        System.out.println("ServiceD is doing something.");
    }
}
