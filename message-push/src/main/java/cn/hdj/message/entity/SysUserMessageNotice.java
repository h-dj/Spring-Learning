package cn.hdj.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
/**
 * <p>
 * 用户消息通知
 * </p>
 *
 * @author ${author}
 * @since 2022-02-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user_message_notice")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysUserMessageNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 消息内容表ID
     */
    private Integer messageContentId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否已读：0否，1是
     */
    private String isRead;

    /**
     * 是否删除：0否，1是
     */
    private String isDelete;

    /**
     * 接收消息用户ID
     */
    private Integer receiverUserId;


}
