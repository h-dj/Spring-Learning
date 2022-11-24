package cn.hdj.xxljob.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/4/28 18:09
 */
@ConfigurationProperties(value = "xxl.job")
@Data
public class XxlJobProperties {

    public static final String LOGIN_IDENTITY_KEY = "XXL_JOB_LOGIN_IDENTITY";

    private XxlJobProperties.Admin admin;
    private XxlJobProperties.Executor executor;

    @Data
    public static class Admin {
        private String addresses;
        private String username;
        private String password;
    }

    @Data
    public static class Executor {
        private String title;
        private String accessToken;
        private String appname;
        private String address;
        private String ip;
        private int port;
        private String logPath;
        private int logRetentionDays;
    }


}
