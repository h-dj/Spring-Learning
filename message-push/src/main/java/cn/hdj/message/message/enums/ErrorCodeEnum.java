package cn.hdj.message.message.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 错误编码枚举
 * @Author huangjiajian
 * @Date 2022/2/25 11:29
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {

    TEMPLATE_ID_MISSING(600, "消息模板ID 为空！"),
    TEMPLATE_VARS_MISSING(601, "消息模板参数 为空！"),
    REQUEST_OBJ_NULL(602, "消息请求为空！"),
    EMAIL_FROM_MISS(603, "邮件消息 from 发送人为空！"),
    EMAIL_TO_MISS(603, "邮件消息 to 接收人为空！"),
    WEB_SEND_USER_MISS(603, "web站内信消息发送人为空！"),
    WEB_RECEIVE_USER_MISS(603, "web站内信接收人为空！"),
    ;

    private int code;
    private String msg;
}
