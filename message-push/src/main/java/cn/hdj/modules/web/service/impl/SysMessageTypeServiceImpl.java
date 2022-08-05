package cn.hdj.modules.web.service.impl;

import cn.hdj.modules.web.domain.from.SysMessageTypeForm;
import cn.hdj.modules.web.domain.from.SysMessageTypeRefForm;
import cn.hdj.modules.web.entity.SysMessageType;
import cn.hdj.modules.web.entity.SysMessageTypeRef;
import cn.hdj.modules.web.mapper.SysMessageTypeMapper;
import cn.hdj.modules.web.service.ISysMessageTypeRefService;
import cn.hdj.modules.web.service.ISysMessageTypeService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 消息模板类型 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
@Service
public class SysMessageTypeServiceImpl extends ServiceImpl<SysMessageTypeMapper, SysMessageType> implements ISysMessageTypeService {

    @Resource
    private ISysMessageTypeRefService sysMessageTypeRefService;


    @Transactional(rollbackFor = Exception.class,propagation = Propagation.NESTED)
    @Override
    public void saveUpdateMessageType(SysMessageTypeForm messageTypeForm) {

        SysMessageType sysMessageType = BeanUtil.copyProperties(messageTypeForm, SysMessageType.class);
        this.saveOrUpdate(sysMessageType);

        LambdaQueryWrapper<SysMessageTypeRef> queryWrapper = Wrappers.<SysMessageTypeRef>lambdaQuery()
                .eq(SysMessageTypeRef::getTempServerId, messageTypeForm.getId());
        this.sysMessageTypeRefService.remove(queryWrapper);

        List<SysMessageTypeRefForm> typeRefFormList = messageTypeForm.getTypeRefFormList();
        List<SysMessageTypeRef> typeRefList = BeanUtil.copyToList(typeRefFormList, SysMessageTypeRef.class);
        this.sysMessageTypeRefService.saveBatch(typeRefList);
    }
}
