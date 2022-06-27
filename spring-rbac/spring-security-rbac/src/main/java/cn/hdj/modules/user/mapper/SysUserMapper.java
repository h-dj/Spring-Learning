package cn.hdj.modules.user.mapper;

import cn.hdj.modules.user.domain.dto.UserAuthDTO;
import cn.hdj.modules.user.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    UserAuthDTO getUserByUserName(@Param("username") String username);
}
