package cn.hdj.client.config;

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

    private Sender server=new Sender();
    private Receiver client=new Receiver();

    @Data
    public class Sender {
        private String privateKey;
        private String publicKey;
    }

    @Data
    public class Receiver {
        private String privateKey;
        private String publicKey;
    }
}
