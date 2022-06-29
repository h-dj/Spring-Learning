package cn.hdj.security.core.userdetails.user;

import cn.hdj.common.api.ResponseCodeEnum;
import cn.hdj.common.enums.AuthenticationIdentityEnum;
import cn.hdj.modules.user.domain.dto.UserAuthDTO;
import cn.hdj.modules.user.service.ISysUserService;
import cn.hdj.security.core.userdetails.user.SysUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @Author huangjiajian
 * @Date 2022/6/26 下午1:15
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Slf4j
@Service(value = "sysUserSmsDetailsService")
public class SysUserSmsDetailServiceImpl implements UserDetailsService {

    @Autowired
    @Lazy
    private  ISysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserDetails userDetails = null;
        UserAuthDTO result = sysUserService.getUserByMobile(username);
        userDetails = new SysUserDetails(result);
        if (userDetails == null) {
            throw new UsernameNotFoundException(ResponseCodeEnum.USER_NOT_EXIST.getMsg());
        } else if (!userDetails.isEnabled()) {
            throw new DisabledException(ResponseCodeEnum.LOCK_ACCOUNT.getMsg());
        } else if (!userDetails.isAccountNonLocked()) {
            throw new LockedException(ResponseCodeEnum.LOCK_ACCOUNT.getMsg());
        } else if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException(ResponseCodeEnum.EXPIRE_ACCOUNT.getMsg());
        }

        userDetails.setAuthenticationIdentity(AuthenticationIdentityEnum.MOBILE.getValue());   // 认证身份标识:mobile
        return userDetails;
    }
}
