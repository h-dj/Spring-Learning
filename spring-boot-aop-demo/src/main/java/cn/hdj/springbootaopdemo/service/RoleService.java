package cn.hdj.springbootaopdemo.service;

import cn.hdj.springbootaopdemo.entity.RoleDO;

import java.util.List;

/**
 * @author hdj
 * @Description:
 * @date 8/30/19
 */
public interface RoleService {

    /**
     * 角色列表
     *
     * @return
     */
    List<RoleDO> listRole();

    /**
     * 添加角色
     *
     * @param role
     */
    void saveRole(RoleDO role);

    /**
     * 更新角色
     *
     * @param roleId
     * @param role
     */
    void updateRole(Long roleId, RoleDO role);

    /**
     * 删除角色
     *
     * @param roleId
     */
    void removeRole(Long roleId);

}
