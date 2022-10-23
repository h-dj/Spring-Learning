package cn.hdj.fastboot.modules.test.strategy;

import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * @Author huangjiajian
 * @Date 2022/10/21 下午10:09
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Component
public class DateNumberSettingTypeStrategy implements INumberSettingTypeStrategy {


    @Override
    public String parse(CrmNumberSetting setting, Map<String, Object> params) {
        String format = setting.getValue();
        return DateUtil.format(new Date(), format);
    }
}
