package cn.hdj.repeatsubmit.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/12/13 下午11:01
 */
@Configuration
public class ZookeeperConfig {

    /**
     * 创建 CuratorFramework 对象并连接 Zookeeper
     *
     * @param zookeeperProperties 从 Spring 容器载入 zookeeperProperties Bean 对象，读取连接 ZK 的参数
     * @return CuratorFramework
     */
    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework(ZookeeperProperties zookeeperProperties) {
        return CuratorFrameworkFactory.newClient(
                zookeeperProperties.getAddress(),
                zookeeperProperties.getSessionTimeoutMs(),
                zookeeperProperties.getConnectionTimeoutMs(),
                new RetryNTimes(zookeeperProperties.getRetryCount(),
                        zookeeperProperties.getElapsedTimeMs()));
    }
}
