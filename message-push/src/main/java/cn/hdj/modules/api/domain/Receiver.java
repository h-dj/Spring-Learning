package cn.hdj.modules.api.domain;

import lombok.Data;

import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/8/5 17:49
 */
@Data
public class Receiver {

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 邮件
     */
    private String email;
    /**
     * 手机号码
     */
    private String phone;
    private String idd = "+86";
    private Map<String, Object> additionInfo;
    private String openUserId;

}
