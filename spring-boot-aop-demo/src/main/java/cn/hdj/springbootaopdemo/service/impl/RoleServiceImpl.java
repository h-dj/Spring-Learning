package cn.hdj.springbootaopdemo.service.impl;

import cn.hdj.springbootaopdemo.entity.RoleDO;
import cn.hdj.springbootaopdemo.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hdj
 * @Description:
 * @date 8/30/19
 */
@Service
public class RoleServiceImpl implements RoleService {


    @Override
    public List<RoleDO> listRole() {
        System.out.println("角色列表");
        return null;
    }

    @Override
    public void saveRole(RoleDO role) {
        System.out.println("添加角色");
    }

    @Override
    public void updateRole(Long roleId, RoleDO role) {
        System.out.println("更新角色");
    }

    @Override
    public void removeRole(Long roleId) {
        System.out.println("删除");
    }
}
