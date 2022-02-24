package cn.hdj.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
/**
 * <p>
 * 系统消息模板
 * </p>
 *
 * @author ${author}
 * @since 2022-02-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_message_template")
public class SysMessageTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 模板类型: 1sms、2email、3 微信公众号消息、4系统站内信
     */
    private String type;

    /**
     * 模板标题
     */
    private String title;
    /**
     * 模板内容
     */
    private String content;

    /**
     * 是否删除: 0否、1是
     */
    private String isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 更新用户ID
     */
    private Integer updateUserId;


}
