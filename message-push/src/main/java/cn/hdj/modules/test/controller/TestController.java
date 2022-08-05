package cn.hdj.modules.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/4/15 15:45
 */
@Tag(name = "操作接口", description = "操作描述")
@RestController
@RequestMapping(value = "test")
public class TestController {

    @Operation(summary = "添加", description = "添加描述")
    @GetMapping("/add")
    public String add() {
        return "ok";
    }

}
