package cn.hdj.fastboot.modules.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 系统自动生成编号配置表
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
@TableName("crm_number_config")
@ApiModel(value = "CrmNumberConfig对象", description = "系统自动生成编号配置表")
public class CrmNumberConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("配置id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("配置名称")
    private String name;

    @ApiModelProperty("配置业务编码")
    private String code;

    @ApiModelProperty("编号顺序")
    private Integer sort;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "CrmNumberConfig{" +
        "id=" + id +
        ", name=" + name +
        ", code=" + code +
        ", sort=" + sort +
        ", createTime=" + createTime +
        ", createUserId=" + createUserId +
        "}";
    }
}
