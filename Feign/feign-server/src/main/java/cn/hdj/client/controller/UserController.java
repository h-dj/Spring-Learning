package cn.hdj.client.controller;

import cn.hdj.client.domain.ApiRequest;
import cn.hdj.client.domain.ApiResponse;
import cn.hdj.client.domain.UserDTO;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 10:02
 */
@RestController
@RequestMapping(value = "/users")
public class UserController {

    @PostMapping(value = "/list")
    public ApiResponse list(@RequestBody ApiRequest request) {

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1222L);
        userDTO.setUserName("Java");
        return new ApiResponse(userDTO);
    }
}
