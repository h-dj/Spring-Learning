package cn.hdj.fastboot.modules.test.service;

import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 系统自动生成编号设置表 服务类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
public interface ICrmNumberSettingService extends IService<CrmNumberSetting> {

    void addNumSetting(CrmNumberSetting config);

    String generate(Map<String, Object> map, String code);
}
