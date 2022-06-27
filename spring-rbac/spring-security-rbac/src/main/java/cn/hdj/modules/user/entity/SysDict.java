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
 * 字典类型表
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
@Getter
@Setter
@TableName("sys_dict")
@ApiModel(value = "SysDict对象", description = "字典类型表")
public class SysDict implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键 ")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("类型名称")
    private String name;

    @ApiModelProperty("类型编码")
    private String code;

    @ApiModelProperty("状态（0-正常 ,1-停用）")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModified;


}
