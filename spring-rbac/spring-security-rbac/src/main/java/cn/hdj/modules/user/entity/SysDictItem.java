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
 * 字典数据表
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
@Getter
@Setter
@TableName("sys_dict_item")
@ApiModel(value = "SysDictItem对象", description = "字典数据表")
public class SysDictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("字典项名称")
    private String name;

    @ApiModelProperty("字典项值")
    private String value;

    @ApiModelProperty("字典编码")
    private String dictCode;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("状态（0 停用 1正常）")
    private Integer status;

    @ApiModelProperty("是否默认（0否 1是）")
    private Integer defaulted;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModified;


}
