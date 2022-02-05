package cn.hdj.scene.controller;

import cn.hdj.scene.entity.UserDTO;
import cn.hdj.scene.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/1/30 下午6:38
 *
 * @see <a>https://www.yuque.com/h_dj/kd0g9f/pefeqn</a>
 */
@RestController
@RequestMapping(value = "/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable("userId") Long userId){
        return this.userService.getUser(userId);
    }


    @PutMapping("/{userId}")
    public String updateUser(@PathVariable("userId") Long userId) throws InterruptedException {
        this.userService.updateById(userId);
        return "ok";
    }
}
