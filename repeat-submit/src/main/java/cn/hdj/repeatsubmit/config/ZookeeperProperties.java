package cn.hdj.repeatsubmit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/12/13 下午11:01
 */
@Configuration
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperProperties {

    /** 重试次数 */
    private int retryCount;

    /** 重试间隔时间 */
    private int elapsedTimeMs;

    /**连接地址 */
    private String address;

    /**Session过期时间 */
    private int sessionTimeoutMs;

    /**连接超时时间 */
    private int connectionTimeoutMs;

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getElapsedTimeMs() {
        return elapsedTimeMs;
    }

    public void setElapsedTimeMs(int elapsedTimeMs) {
        this.elapsedTimeMs = elapsedTimeMs;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }
}