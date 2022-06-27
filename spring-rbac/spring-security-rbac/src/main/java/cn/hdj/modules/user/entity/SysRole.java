package cn.hdj.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
@Getter
@Setter
@TableName("sys_role")
@ApiModel(value = "SysRole对象", description = "角色表")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("角色名称")
    private String name;

    @ApiModelProperty("角色编码")
    private String code;

    @ApiModelProperty("显示顺序")
    private Integer sort;

    @ApiModelProperty("角色状态：0-正常；1-停用")
    private Integer status;

    @ApiModelProperty("逻辑删除标识：0-未删除；1-已删除")
    private Integer deleted;

    @ApiModelProperty("更新时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty("创建时间")
    private LocalDateTime gmtModified;


}
