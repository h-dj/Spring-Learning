package cn.reid.springjmsibm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseMessageDTO {

    /** 消息在队列中的位置（序号） */
    private int position;

    /** 消息ID（去掉 "ID:" 前缀的十六进制字符串） */
    private String messageId;

    /** 发送用户ID（MQMD PutUserId / JMSXUserID） */
    private String putUserId;

    /** 发送应用名称（MQMD PutApplName / JMSXAppID） */
    private String putApplName;

    /** 消息写入队列的时间，格式 yyyy-MM-dd HH:mm:ss（UTC） */
    private String timestamp;

    /** 消息体大小（字节） */
    private int messageSize;

    /** 消息体内容的 Base64 编码，超过 100KB 时截断 */
    private String bodyBase64;

    /** 关联ID（去掉 "ID:" 前缀的十六进制字符串），用于追踪请求-回复对 */
    private String correlId;

    /** 回复队列名，用于标识回复消息应投递到的目标队列 */
    private String replyToQueueName;

    /** 回复队列管理器名 */
    private String replyToQueueManager;

    public int backoutCount;
    public int priority;
}
