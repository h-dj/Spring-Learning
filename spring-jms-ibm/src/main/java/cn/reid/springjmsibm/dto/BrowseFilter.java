package cn.reid.springjmsibm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrowseFilter {

    /** 返回条数上限，默认 20，最大 200 */
    @Builder.Default
    private int limit = 20;

    /** 起始位置，默认 0 */
    @Builder.Default
    private int offset = 0;

    /** 消息ID前缀匹配（去掉 "ID:" 前缀） */
    private String msgId;

    /** 发送用户 ID（PutUserId / JMSXUserID） */
    private String putUserId;

    /** 发送应用名（PutApplName / JMSXAppID） */
    private String putApplName;

    /** 开始时间 yyyy-MM-dd HH:mm:ss（UTC），消息写入时间 >= 该值 */
    private String startTime;

    /** 结束时间 yyyy-MM-dd HH:mm:ss（UTC），消息写入时间 <= 该值 */
    private String endTime;

    public int getLimit() {
        return Math.min(Math.max(limit, 1), 200);
    }
}
