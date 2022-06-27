package cn.hdj.modules.user.mapper;

import cn.hdj.modules.user.domain.dto.SysPermissionDTO;
import cn.hdj.modules.user.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 权限表 Mapper 接口
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    /**
     * 权限<->有权限的角色集合
     *
     * @return
     */
    List<SysPermissionDTO> listPermRoles();
}
