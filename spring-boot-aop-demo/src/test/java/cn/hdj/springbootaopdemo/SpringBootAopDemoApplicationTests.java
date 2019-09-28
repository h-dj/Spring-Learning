package cn.hdj.springbootaopdemo;


import cn.hdj.springbootaopdemo.entity.RoleDO;
import cn.hdj.springbootaopdemo.entity.UserDO;
import cn.hdj.springbootaopdemo.service.Impl2.RoleServiceImpl2;
import cn.hdj.springbootaopdemo.service.RoleService;
import cn.hdj.springbootaopdemo.service.UserService;
import cn.hdj.springbootaopdemo.service.impl.RoleServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootAopDemoApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    private UserService userService;

    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    private RoleServiceImpl2 roleService2;

    @Test
    public void testUser() {
        List<UserDO> list = userService.listUser();
        System.out.println("返回值：" + list.size());
        userService.saveUser(null);
        userService.updateUser(null, null);
        userService.removeUser(null);
    }

    @Test
    public void testRole() {
        roleService.listRole();
        roleService.saveRole(null);
        roleService.updateRole(null, null);
        roleService.removeRole(null);
    }

    @Test
    public void testRole2() {
        List<RoleDO> roleDOS = roleService2.listRole();
        roleService2.saveRole(null);
        roleService2.updateRole(null, null);
        roleService2.removeRole(null);
    }

}
