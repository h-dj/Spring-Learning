package cn.hdj.security.extension;

import cn.hdj.common.dto.LoginUserDTO;
import cn.hutool.core.util.StrUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * @Author huangjiajian
 * @Date 2022/6/26 下午6:49
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
public class AuthenticationTokenFactory {


    public static Authentication getToken(LoginUserDTO loginUserDTO) {
        if (StrUtil.isNotBlank(loginUserDTO.getUsername()) && StrUtil.isNotBlank(loginUserDTO.getPassword())) {
            return new UsernamePasswordAuthenticationToken(loginUserDTO.getUsername(), loginUserDTO.getPassword());
        }
        throw new IllegalArgumentException("找不到合适的验证token!");
    }


}
