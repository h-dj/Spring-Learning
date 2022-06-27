package cn.hdj.common.dto;

import lombok.Data;

/**
 * @author huangjiajian
 */
@Data
public class LoginUserDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;
    /**
     * 手机验证码
     */
    private String verificationCode;

    /**
     * 图片验证码
     */
    private String imageVerificationCode;
    /**
     * 图片验证码key
     */
    private String imageVerificationCodeKey;

}
