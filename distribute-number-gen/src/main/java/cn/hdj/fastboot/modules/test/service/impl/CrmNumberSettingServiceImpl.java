package cn.hdj.fastboot.modules.test.service.impl;

import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import cn.hdj.fastboot.modules.test.enums.CrmNumberSettingTypeEnum;
import cn.hdj.fastboot.modules.test.mapper.CrmNumberSettingMapper;
import cn.hdj.fastboot.modules.test.service.ICrmNumberSettingService;
import cn.hdj.fastboot.modules.test.strategy.INumberSettingTypeStrategy;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 系统自动生成编号设置表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
@Service
@AllArgsConstructor
public class CrmNumberSettingServiceImpl extends ServiceImpl<CrmNumberSettingMapper, CrmNumberSetting> implements ICrmNumberSettingService {

    private final RedisTemplate<String, Object> redisTemplate;



    @Override
    public void addNumSetting(CrmNumberSetting config) {
        this.save(config);
    }

    @Override
    public String generate(Map<String, Object> map, String code) {

        BoundValueOperations<String, Object> valueOps = this.redisTemplate.boundValueOps("number_setting_" + code);
        Object o = valueOps.get();

        List<CrmNumberSetting> list =null;
        if(o == null){
            //通过业务编码查询 编号设置
            LambdaQueryWrapper<CrmNumberSetting> queryWrapper = Wrappers.<CrmNumberSetting>lambdaQuery()
                    .eq(CrmNumberSetting::getCode, code)
                    .orderByDesc(CrmNumberSetting::getSort);
            list = this.list(queryWrapper);
            valueOps.set(list,1, TimeUnit.DAYS);
        }else {
            list = (List<CrmNumberSetting>) o;
        }

        //按照编号设置，组装
        StringBuilder builder = new StringBuilder(32);
        for (CrmNumberSetting crmNumberSetting : list) {
            Integer type = crmNumberSetting.getType();
            Class<INumberSettingTypeStrategy> strategy = CrmNumberSettingTypeEnum.getStrategy(type);
            INumberSettingTypeStrategy settingTypeStrategy = SpringUtil.getBean(strategy);
            String item = settingTypeStrategy.parse(crmNumberSetting, map);
            builder.append(item);
        }
        return builder.toString();
    }

    @Override
    public long updateCAS(CrmNumberSetting setting) {
        String code = setting.getCode();
        //重新编号周期 1每天 2每月 3每年 4从不
        Integer resetType = setting.getResetType();
        Integer increaseNumber = setting.getIncreaseNumber();

        String key = DateUtil.format(new Date(), "yyyyMM");

        Long increment = this.redisTemplate.boundHashOps(code)
                .increment(key, increaseNumber);

//        CrmNumberSetting temp = new CrmNumberSetting();
//        temp.setCode(code);
//        temp.setId(setting.getId());
//        temp.setLastNumber(increment.intValue());
//        temp.setLastDate(LocalDate.now());
//
//        this.updateById(temp);

        return increment;
    }
}
