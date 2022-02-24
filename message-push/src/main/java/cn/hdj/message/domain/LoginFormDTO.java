package cn.hdj.message.domain;

import lombok.Data;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2022/2/23 16:30
 */
@Data
public class LoginFormDTO implements java.io.Serializable{

    private String username;
    private String password;

}
