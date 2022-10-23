package cn.hdj.fastboot.modules.test.strategy;

import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author huangjiajian
 * @Date 2022/10/21 下午10:09
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Component
public interface INumberSettingTypeStrategy {

    String parse(CrmNumberSetting setting, Map<String, Object> params);
}
