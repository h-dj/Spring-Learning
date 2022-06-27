package cn.hdj.modules.user.service;

import cn.hdj.modules.user.domain.dto.SysPermissionDTO;
import cn.hdj.modules.user.entity.SysPermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 权限表 服务类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
public interface ISysPermissionService extends IService<SysPermission> {

    List<SysPermissionDTO> listPermRoles();
}
