package cn.hdj.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证身份标识枚举
 *
 * @author haoxr
 * @date 2021/10/4
 */
@Getter
@AllArgsConstructor
public enum AuthenticationIdentityEnum {

    USERNAME("username", "用户名"),
    MOBILE("mobile", "手机号"),
    OPENID("openId", "开放式认证系统唯一身份标识");

    private String value;
    private String label;

}
