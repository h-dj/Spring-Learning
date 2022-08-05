package cn.hdj.modules.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户消息配置
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
@TableName("sys_user_message_config")
@ApiModel(value = "SysUserMessageConfig对象", description = "用户消息配置")
public class SysUserMessageConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty("更新人")
    private String updatedBy;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedTime;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("消息编码")
    private String messageCode;

    @ApiModelProperty("是否发送站内信")
    private String web;

    @ApiModelProperty("是否发送短信")
    private String sms;

    @ApiModelProperty("是否发送邮件")
    private String email;

    @ApiModelProperty("是否发送微信公众号")
    private String officialWechat;


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOfficialWechat() {
        return officialWechat;
    }

    public void setOfficialWechat(String officialWechat) {
        this.officialWechat = officialWechat;
    }

    @Override
    public String toString() {
        return "SysUserMessageConfig{" +
        "createdBy=" + createdBy +
        ", createdTime=" + createdTime +
        ", updatedBy=" + updatedBy +
        ", updatedTime=" + updatedTime +
        ", id=" + id +
        ", userId=" + userId +
        ", messageCode=" + messageCode +
        ", web=" + web +
        ", sms=" + sms +
        ", email=" + email +
        ", officialWechat=" + officialWechat +
        "}";
    }
}
