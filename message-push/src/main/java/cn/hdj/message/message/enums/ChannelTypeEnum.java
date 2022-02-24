package cn.hdj.message.message.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Description: 发送渠道
 * @Author huangjiajian
 * @Date 2022/2/24 11:34
 */
@Getter
@ToString
@AllArgsConstructor
public enum ChannelTypeEnum {


    SYS_MSG(10, "sys_msg(站内信)", "sys_msg"),
    SMS(20, "sms(短信)", "sms"),
    EMAIL(30, "email(邮件)", "email"),
    WECHAT_OFFICIAL_ACCOUNTS(40, "WeChatOfficialAccounts(微信服务号)", "We_chat_official_accounts"),
    MINI_PROGRAM(50, "miniProgram(小程序)", "mini_program"),
    ;

    /**
     * 编码值
     */
    private Integer code;
    /**
     * 描述
     */
    private String description;
    /**
     * 英文标识
     */
    private String codeEn;

}

