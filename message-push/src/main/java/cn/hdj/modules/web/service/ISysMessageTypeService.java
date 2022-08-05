package cn.hdj.modules.web.service;

import cn.hdj.modules.web.domain.from.SysMessageTypeForm;
import cn.hdj.modules.web.entity.SysMessageType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 消息模板类型 服务类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
public interface ISysMessageTypeService extends IService<SysMessageType> {

    /**
     * 保存更新 消息类型
     *
     * @param messageTypeForm
     */
    void saveUpdateMessageType(SysMessageTypeForm messageTypeForm);
}
