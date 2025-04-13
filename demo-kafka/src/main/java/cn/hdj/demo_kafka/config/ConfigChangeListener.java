package cn.hdj.demo_kafka.config;

import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConfigChangeListener {

    @ApolloConfigChangeListener(value = {"application","TEST1.common"})
    public void onChange(com.ctrip.framework.apollo.model.ConfigChangeEvent changeEvent) {
        changeEvent.changedKeys().forEach(key -> {
            log.info("Changed key: " + key + ", new value: " + changeEvent.getChange(key).getNewValue());
        });
    }
}
