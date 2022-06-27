package cn.hdj.modules.user.service;

import cn.hdj.common.dto.LoginUserDTO;
import cn.hdj.modules.user.domain.dto.UserAuthDTO;
import cn.hdj.modules.user.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 通过用户名查询用户
     *
     * @param username
     * @return
     */
    UserAuthDTO getUserByUsername(String username);

    /**
     * 登录
     *
     * @param loginUserDTO
     * @return
     */
    String login(LoginUserDTO loginUserDTO);

    /**
     * 刷新token
     *
     * @param token
     * @return
     */
    String refreshToken(String token);
}
