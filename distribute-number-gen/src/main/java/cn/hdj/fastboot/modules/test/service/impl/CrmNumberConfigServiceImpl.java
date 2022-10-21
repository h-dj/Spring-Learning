package cn.hdj.fastboot.modules.test.service.impl;

import cn.hdj.fastboot.common.api.ResponseCodeEnum;
import cn.hdj.fastboot.common.exception.BaseException;
import cn.hdj.fastboot.modules.test.entity.CrmNumberConfig;
import cn.hdj.fastboot.modules.test.mapper.CrmNumberConfigMapper;
import cn.hdj.fastboot.modules.test.service.ICrmNumberConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统自动生成编号配置表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
@Service
public class CrmNumberConfigServiceImpl extends ServiceImpl<CrmNumberConfigMapper, CrmNumberConfig> implements ICrmNumberConfigService {


    @Override
    public void addNumConfig(CrmNumberConfig config) {
        //查询 业务编码是否重复
        LambdaQueryWrapper<CrmNumberConfig> queryWrapper = Wrappers.<CrmNumberConfig>lambdaQuery()
                .eq(CrmNumberConfig::getCode, config.getCode())
                .ne(config.getId() != null, CrmNumberConfig::getId, config.getId());

        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BaseException("CrmNumberConfig::getCode 业务编码重复！", 400);
        }
        this.save(config);
    }
}
