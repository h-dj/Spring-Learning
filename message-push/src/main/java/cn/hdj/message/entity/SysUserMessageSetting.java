package cn.hdj.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
/**
 * <p>
 * 用户消息提醒设置
 * </p>
 *
 * @author ${author}
 * @since 2022-02-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user_message_setting")
public class SysUserMessageSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息设置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 邮件发送：0关闭、1开启
     */
    private String email;

    /**
     * 短信发送：0关闭、1开启
     */
    private String sms;

    /**
     * 系统站内信：0关闭、1开启
     */
    private String web;

    /**
     * 微信公众号消息：0关闭、1开启
     */
    private String wechat;

    /**
     * 消息类型编码
     */
    private String messageTypeCode;

    /**
     * 用户ID
     */
    private Integer userId;


}
