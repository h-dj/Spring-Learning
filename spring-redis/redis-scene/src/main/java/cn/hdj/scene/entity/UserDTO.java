package cn.hdj.scene.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/1/30 下午11:39
 */
@Getter
@Setter
public class UserDTO implements Serializable {

    private Long userId;
    private List<String> roles;
}
