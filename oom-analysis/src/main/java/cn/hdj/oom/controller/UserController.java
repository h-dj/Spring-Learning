package cn.hdj.oom.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author huangjiajian
 * @Date 2022/8/13 下午5:59
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@RequestMapping(value = "/api/user")
@RestController
public class UserController {



    @GetMapping(value = "/test")
    public String test() {
        return "ok";
    }
}
