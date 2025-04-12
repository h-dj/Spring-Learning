package cn.hdj.springcircularreferences;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author hdj
 * @date 2023/10/17 16:23
 * @description 代理引入的额外挑战
 * 当 Bean 被代理（例如应用了 AOP、声明式事务等），Spring 的实例化流程会变得更为复杂，因为代理对象在创建时对原对象进行了包装。通常涉及两种代理模式：
 * <p>
 * JDK 动态代理：仅适用于实现了接口的 Bean
 * <p>
 * CGLIB 代理：通过继承方式生成代理类
 * <p>
 * 代理机制会在 Bean 初始化过程中产生额外的依赖关系，可能会干扰 Spring 的早期引用机制，从而导致以下两种常见问题：
 * <p>
 * ① 早期引用丢失
 * 当 Bean 被代理之后，Spring 内部保存的引用可能指向原始对象，而依赖注入时却拿到代理对象，造成对象状态不一致，从而在循环依赖场景下，代理 Bean 还未正确创建或初始化。
 * <p>
 * ② 代理对象与原始对象混用
 * 在某些场景中，当一个 Bean 的依赖既引用了被代理的对象又引用了原始对象（未代理的实例），这会导致状态混乱，特别是在事务、缓存等敏感场景下，引起不可预期的行为或抛出异常。
 *
 *
 *
 *
 */
@Service
public class ServiceC {

    @Autowired
    @Lazy  // 延迟加载，避免循环依赖
    private ServiceD serviceD;

    @Async
    public void doSomething() {
        System.out.println("ServiceC is doing something.");

        serviceD.doSomething();
    }
}
