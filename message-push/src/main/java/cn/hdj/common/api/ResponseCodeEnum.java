package cn.hdj.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/4/15 14:25
 */
@AllArgsConstructor
@Getter
public enum ResponseCodeEnum {

    //响应成功
    SUCCESS(200, "操作成功"),

    UNKNOWN(500, "系统内部错误，请联系管理员"),
    PATH_NOT_FOUND(404, "路径不存在，请检查路径"),
    NO_AUTH(401, "抱歉，您没有访问权限！"),


    // 系统业务状态码(六位数)：  100001
    LOGIN_FAIL(100001, "登录失败"),
    CAPTCHA_WRONG(100002, "验证码错误"),
    USERNAME_OR_PASSWORD_WRONG(100003, "用户名或密码错误！"),
    LOCK_ACCOUNT(100004, "账户被锁定或禁用,请稍后再试！");

    private Integer code;
    private String msg;

}
