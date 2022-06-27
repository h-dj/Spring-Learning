package cn.hdj.security.core.userdetails.user;

import cn.hdj.common.enums.AuthenticationIdentityEnum;
import cn.hdj.common.consts.GlobalConstants;
import cn.hdj.common.enums.PasswordEncoderTypeEnum;
import cn.hdj.modules.user.domain.dto.UserAuthDTO;
import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author huangjiajain
 */
@Data
public class SysUserDetails implements UserDetails {

    /**
     * 扩展字段：用户ID
     */
    private Long userId;

    /**
     * 扩展字段：认证身份标识，枚举值如下：
     *
     * @see  AuthenticationIdentityEnum
     */
    private String authenticationIdentity;

    /**
     * 扩展字段：部门ID
     */
    private Long deptId;

    /**
     * 默认字段
     */
    private String username;
    private String password;
    private Boolean enabled;
    private Collection<SimpleGrantedAuthority> authorities;

    /**
     * 系统管理用户
     */
    public SysUserDetails(UserAuthDTO user) {
        this.setUserId(user.getUserId());
        this.setUsername(user.getUsername());
        this.setDeptId(user.getDeptId());
        this.setPassword(PasswordEncoderTypeEnum.BCRYPT.getPrefix() + user.getPassword());
        this.setEnabled(GlobalConstants.STATUS_YES.equals(user.getStatus()));
        if (CollectionUtil.isNotEmpty(user.getRoles())) {
            authorities = new ArrayList<>();
            user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
