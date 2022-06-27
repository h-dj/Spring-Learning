package cn.hdj.modules.user.service.impl;

import cn.hdj.modules.user.domain.dto.SysPermissionDTO;
import cn.hdj.modules.user.entity.SysPermission;
import cn.hdj.modules.user.mapper.SysPermissionMapper;
import cn.hdj.modules.user.service.ISysPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 权限表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {


    @Override
    public List<SysPermissionDTO> listPermRoles() {
        return this.baseMapper.listPermRoles();
    }
}
