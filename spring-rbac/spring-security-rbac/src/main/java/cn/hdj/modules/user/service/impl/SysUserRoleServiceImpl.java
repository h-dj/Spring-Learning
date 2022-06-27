package cn.hdj.modules.user.service.impl;

import cn.hdj.modules.user.entity.SysUserRole;
import cn.hdj.modules.user.mapper.SysUserRoleMapper;
import cn.hdj.modules.user.service.ISysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户和角色关联表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

}
