package cn.hdj.mvc.modules.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/6/15 18:27
 */
@RestController
public class UserController {

    @GetMapping(value = "/test")
    public String test() {
        return "ok";
    }
}
