package cn.reid.springjmsibm.config;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Hashtable;

@Slf4j
@Configuration
public class MqConnectionConfig {

    @Value("${ibm.mq.queue-manager}")
    private String queueManager;

    @Value("${ibm.mq.conn-name}")
    private String connName;

    @Value("${ibm.mq.channel}")
    private String channel;

    @Value("${ibm.mq.user}")
    private String user;

    @Value("${ibm.mq.password}")
    private String password;

    @Bean(destroyMethod = "disconnect")
    public MQQueueManager mqQueueManager() throws MQException {
        String host = connName.substring(0, connName.indexOf('('));
        String portStr = connName.substring(connName.indexOf('(') + 1, connName.indexOf(')'));

        Hashtable<String, Object> props = new Hashtable<>();
        props.put(CMQC.HOST_NAME_PROPERTY, host);
        props.put(CMQC.PORT_PROPERTY, Integer.parseInt(portStr));
        props.put(CMQC.CHANNEL_PROPERTY, channel);
        props.put(CMQC.USER_ID_PROPERTY, user);
        props.put(CMQC.PASSWORD_PROPERTY, password);
        props.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);

        MQQueueManager qmgr = new MQQueueManager(queueManager, props);
        log.info("MQQueueManager connected to: {}", queueManager);
        return qmgr;
    }
}
