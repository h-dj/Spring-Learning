package cn.hdj.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 17:27
 */
@ConfigurationProperties(prefix = "secure")
@Data
public class SecureProperties {

    private Sender sender;
    private Receiver receiver;

    @Data
    class Sender {
        private String privateKey;
        private String publicKey;
    }

    @Data
    class Receiver {
        private String privateKey;
        private String publicKey;
    }
}
