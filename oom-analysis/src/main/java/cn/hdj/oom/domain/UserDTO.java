package cn.hdj.oom.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author huangjiajian
 * @Date 2022/8/14 下午3:09
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Data
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
}
