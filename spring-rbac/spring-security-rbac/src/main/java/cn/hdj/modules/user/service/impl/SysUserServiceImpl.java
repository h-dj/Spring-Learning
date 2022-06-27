package cn.hdj.modules.user.service.impl;

import cn.hdj.security.extension.AuthenticationTokenFactory;
import cn.hdj.common.dto.LoginUserDTO;
import cn.hdj.modules.user.domain.dto.UserAuthDTO;
import cn.hdj.modules.user.entity.SysUser;
import cn.hdj.modules.user.mapper.SysUserMapper;
import cn.hdj.modules.user.service.ISysUserService;
import cn.hdj.security.core.userdetails.user.SysUserDetails;
import cn.hdj.security.utils.JwtTokenUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    /**
     * 验证管理器
     */
    private final AuthenticationManager authenticationManager;

    @Override
    public UserAuthDTO getUserByUsername(String username) {
        return this.baseMapper.getUserByUserName(username);
    }

    @Override
    public String login(LoginUserDTO loginUserDTO) {

        //获取登录token
        Authentication token = AuthenticationTokenFactory.getToken(loginUserDTO);
        Authentication authenticate = this.authenticationManager.authenticate(token);
        SysUserDetails sysUserDetails = (SysUserDetails) authenticate.getPrincipal();
        return JwtTokenUtil.generateToken(sysUserDetails);
    }

    @Override
    public String refreshToken(String oldToken) {
        return JwtTokenUtil.refreshHeadToken(oldToken);
    }
}
