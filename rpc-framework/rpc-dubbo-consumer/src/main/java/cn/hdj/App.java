package cn.hdj;

import cn.hdj.service.GreetingsService;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/8 上午11:59
 * @description:
 */
public class App {

    private static String zookeeperHost = System.getProperty("zookeeper.address", "127.0.0.1");

    public static void main(String[] args) {
        //当前应用配置
        ApplicationConfig applicationConfig = new ApplicationConfig("first-dubbo-consumer");
        //连接注册中心配置
        RegistryConfig registryConfig = new RegistryConfig("zookeeper://" + zookeeperHost + ":2181");

        //引用远程服务
        ReferenceConfig<GreetingsService> reference = new ReferenceConfig<>();
        reference.setApplication(applicationConfig);
        reference.setRegistry(registryConfig);
        reference.setInterface(GreetingsService.class);
        GreetingsService service = reference.get();
        String message = service.sayHi("dubbo");
        System.out.println(message);
    }
}
