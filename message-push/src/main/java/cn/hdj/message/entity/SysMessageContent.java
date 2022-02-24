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
 * 消息内容表
 * </p>
 *
 * @author ${author}
 * @since 2022-02-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_message_content")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysMessageContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 消息内容
     */
    private String context;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 发送者ID
     */
    private Integer sendUserId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息类型: 1公告，2通知
     */
    private String messageType;

    /**
     * 是否删除: 0否，1是
     */
    private String isDelete;

    /**
     * 消息来源模块
     */
    private Integer moduleId;


}
