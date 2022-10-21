package cn.hdj.fastboot.modules.test.service.impl;

import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import cn.hdj.fastboot.modules.test.mapper.CrmNumberSettingMapper;
import cn.hdj.fastboot.modules.test.service.ICrmNumberSettingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统自动生成编号设置表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
@Service
public class CrmNumberSettingServiceImpl extends ServiceImpl<CrmNumberSettingMapper, CrmNumberSetting> implements ICrmNumberSettingService {

    @Override
    public void addNumSetting(CrmNumberSetting config) {
        this.save(config);
    }

    @Override
    public String generate(Map<String, Object> map, String code) {
        //通过业务编码查询 编号设置
        LambdaQueryWrapper<CrmNumberSetting> queryWrapper = Wrappers.<CrmNumberSetting>lambdaQuery()
                .eq(CrmNumberSetting::getCode, code)
                .orderByDesc(CrmNumberSetting::getSort);

        List<CrmNumberSetting> list = this.list(queryWrapper);

        //按照编号设置，组装
        StringBuilder builder=new StringBuilder();
        for (CrmNumberSetting crmNumberSetting : list) {
            Integer type = crmNumberSetting.getType();


        }
        return builder.toString();
    }
}
