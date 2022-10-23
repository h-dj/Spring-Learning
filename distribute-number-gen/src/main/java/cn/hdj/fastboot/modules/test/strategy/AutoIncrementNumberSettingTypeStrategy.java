package cn.hdj.fastboot.modules.test.strategy;

import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import cn.hdj.fastboot.modules.test.service.ICrmNumberSettingService;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author huangjiajian
 * @Date 2022/10/21 下午10:09
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Component
@AllArgsConstructor
public class AutoIncrementNumberSettingTypeStrategy implements INumberSettingTypeStrategy {

    private final ICrmNumberSettingService crmNumberSettingService;

    @Override
    public String parse(CrmNumberSetting setting, Map<String, Object> params) {
        long num = this.crmNumberSettingService.updateCAS(setting);
        return StrUtil.fillBefore(NumberUtil.toStr(num), '0', 4);
    }
}
