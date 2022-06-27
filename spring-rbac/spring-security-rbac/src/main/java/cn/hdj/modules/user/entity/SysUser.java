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
 * 用户信息表
 * </p>
 *
 * @author huangjiajian
 * @since 2022-06-26
 */
@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "SysUser对象", description = "用户信息表")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("性别：1-男 2-女")
    private Integer gender;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("部门ID")
    private Integer deptId;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("联系方式")
    private String mobile;

    @ApiModelProperty("用户状态：1-正常 0-禁用")
    private Integer status;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("逻辑删除标识：0-未删除；1-已删除")
    private Integer deleted;

    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModified;


}
