package cn.hdj.modules.user.controller;

import cn.hdj.common.api.ResultVO;
import cn.hdj.common.consts.GlobalConstants;
import cn.hdj.common.dto.LoginUserDTO;
import cn.hdj.modules.user.service.ISysUserService;
import cn.hdj.security.utils.JwtTokenUtil;
import cn.hutool.core.codec.Base62;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonCodecWrapper;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @Author huangjiajian
 * @Date 2022/6/26 下午6:37
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Api(tags = "登录接口", value = "登录接口")
@RestController
@Slf4j
@RequestMapping(value = "/oauth")
public class SysLoginController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private RedissonClient redissonClient;


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


    @ApiOperation(value = "手机短信验证码")
    @PostMapping("/sendVerificationCode")
    public ResultVO sendVerificationCode(@RequestParam("mobile") String mobile) {

        //生成随机码
        String randomInt = RandomUtil.randomNumbers(6);
        String codeKey = GlobalConstants.SMS_CODE_PREFIX + mobile;
        this.redissonClient.getBucket(codeKey).set(randomInt,30,TimeUnit.MINUTES);
        return ResultVO.successJson("发送成功！");
    }
}
