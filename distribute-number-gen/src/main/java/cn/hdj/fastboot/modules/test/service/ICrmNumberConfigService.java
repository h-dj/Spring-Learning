package cn.hdj.fastboot.modules.test.service;

import cn.hdj.fastboot.modules.test.entity.CrmNumberConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统自动生成编号配置表 服务类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
public interface ICrmNumberConfigService extends IService<CrmNumberConfig> {

    void addNumConfig(CrmNumberConfig config);
}
