package cn.hdj.modules.user.domain.dto;

import cn.hdj.modules.user.entity.SysPermission;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author huangjiajian
 * @Date 2022/6/27 下午9:15
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Getter
@Setter
public class SysPermissionDTO extends SysPermission {

    private List<String> roles;
}
