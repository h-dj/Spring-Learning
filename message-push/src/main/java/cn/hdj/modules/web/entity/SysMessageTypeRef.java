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
 * 消息模板类型关联
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
@TableName("sys_message_type_ref")
@ApiModel(value = "SysMessageTypeRef对象", description = "消息模板类型关联")
public class SysMessageTypeRef implements Serializable {

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

    @ApiModelProperty("消息模板编码")
    private String templateCode;

    @ApiModelProperty("模板类型ID;sys_template_server.id")
    private Long tempServerId;

    @ApiModelProperty("模版类型;WEB、SMS、EMAIL")
    private String typeCode;

    @ApiModelProperty("启用标识")
    private String enabledFlag;


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

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Long getTempServerId() {
        return tempServerId;
    }

    public void setTempServerId(Long tempServerId) {
        this.tempServerId = tempServerId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getEnabledFlag() {
        return enabledFlag;
    }

    public void setEnabledFlag(String enabledFlag) {
        this.enabledFlag = enabledFlag;
    }

    @Override
    public String toString() {
        return "SysMessageTypeRef{" +
        "createdBy=" + createdBy +
        ", createdTime=" + createdTime +
        ", updatedBy=" + updatedBy +
        ", updatedTime=" + updatedTime +
        ", id=" + id +
        ", templateCode=" + templateCode +
        ", tempServerId=" + tempServerId +
        ", typeCode=" + typeCode +
        ", enabledFlag=" + enabledFlag +
        "}";
    }
}
