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
 * 部门表
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
@Getter
@Setter
@TableName("sys_dept")
@ApiModel(value = "SysDept对象", description = "部门表")
public class SysDept implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("部门名称")
    private String name;

    @ApiModelProperty("父节点id")
    private Long parentId;

    @ApiModelProperty("父节点id路径")
    private String treePath;

    @ApiModelProperty("显示顺序")
    private Integer sort;

    @ApiModelProperty("状态：1-正常 0-禁用")
    private Integer status;

    @ApiModelProperty("删除状态：1-删除 0-未删除")
    private Integer deleted;

    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModified;


}
