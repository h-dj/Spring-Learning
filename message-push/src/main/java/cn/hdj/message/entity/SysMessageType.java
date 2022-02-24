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
 * 消息类型
 * </p>
 *
 * @author ${author}
 * @since 2022-02-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_message_type")
public class SysMessageType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 消息分类：1公告、2、通知
     */
    private String category;

    /**
     * 消息类型名称
     */
    private String name;

    /**
     * 消息类型编码
     */
    private String messageTypeCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除: 0否、1是
     */
    private String isDelete;


}
