package cn.hdj.fastboot.modules.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 系统自动生成编号设置表
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
@TableName("crm_number_setting")
@ApiModel(value = "CrmNumberSetting对象", description = "系统自动生成编号设置表")
public class CrmNumberSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("设置id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("配置业务编码")
    private String code;

    @ApiModelProperty("编号顺序")
    private Integer sort;

    @ApiModelProperty("编号类型 1文本 2日期 3数字 4json映射")
    private Integer type;

    @ApiModelProperty("文本内容或日期格式或起始编号或json数据格式")
    private String value;

    @ApiModelProperty("递增数")
    private Integer increaseNumber;

    @ApiModelProperty("重新编号周期 1每天 2每月 3每年 4从不")
    private Integer resetType;

    @ApiModelProperty("上次生成的编号")
    private Integer lastNumber;

    @ApiModelProperty("上次生成的时间")
    private LocalDate lastDate;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("创建人id")
    private Long createUserId;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getIncreaseNumber() {
        return increaseNumber;
    }

    public void setIncreaseNumber(Integer increaseNumber) {
        this.increaseNumber = increaseNumber;
    }

    public Integer getResetType() {
        return resetType;
    }

    public void setResetType(Integer resetType) {
        this.resetType = resetType;
    }

    public Integer getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(Integer lastNumber) {
        this.lastNumber = lastNumber;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public void setLastDate(LocalDate lastDate) {
        this.lastDate = lastDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    @Override
    public String toString() {
        return "CrmNumberSetting{" +
        "id=" + id +
        ", code=" + code +
        ", sort=" + sort +
        ", type=" + type +
        ", value=" + value +
        ", increaseNumber=" + increaseNumber +
        ", resetType=" + resetType +
        ", lastNumber=" + lastNumber +
        ", lastDate=" + lastDate +
        ", createTime=" + createTime +
        ", createUserId=" + createUserId +
        "}";
    }
}
