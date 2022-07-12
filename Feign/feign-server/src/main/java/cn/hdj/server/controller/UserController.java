package cn.hdj.server.controller;

import cn.hdj.server.domain.ApiRequest;
import cn.hdj.server.domain.ApiResponse;
import cn.hdj.server.domain.UserDTO;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 10:02
 */
@RestController
@RequestMapping(value = "/users")
public class UserController {

    @PostMapping
    public ApiResponse list(@RequestBody ApiRequest request) {

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1222L);
        userDTO.setUserName("Java");
        return new ApiResponse(userDTO);
    }
}
