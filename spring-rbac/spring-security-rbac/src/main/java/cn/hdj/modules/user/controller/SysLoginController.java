package cn.hdj.modules.user.controller;

import cn.hdj.common.api.ResultVO;
import cn.hdj.common.dto.LoginUserDTO;
import cn.hdj.modules.user.service.ISysUserService;
import cn.hdj.security.utils.JwtTokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author huangjiajian
 * @Date 2022/6/26 下午6:37
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Api(tags = "登录接口",value = "登录接口")
@RestController
@Slf4j
@RequestMapping(value = "/oauth")
public class SysLoginController {

    @Autowired
    private ISysUserService sysUserService;


    @ApiOperation(value = "登录-返回授权token")
    @PostMapping(value = "/login")
    public ResultVO login(@RequestBody LoginUserDTO loginUserDTO) {
        String token = this.sysUserService.login(loginUserDTO);
        return ResultVO.successJson(token);
    }


    @ApiOperation(value = "刷新token")
    @GetMapping(value = "/refreshToken")
    public ResultVO refreshToken(HttpServletRequest request) {
        String token = request.getHeader(JwtTokenUtil.tokenHeader);
        String refreshToken = this.sysUserService.refreshToken(token);
        return ResultVO.successJson(refreshToken);
    }

    @ApiOperation(value = "注销")
    @DeleteMapping("/logout")
    public ResultVO logout() {

        return ResultVO.successJson("ok");
    }
}
