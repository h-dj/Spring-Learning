package cn.reid.springjmsibm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueMetricsDTO {
    private String queueName;
    private int currentDepth;
    private int maxDepth;
    private int openInputCount;
    private int openOutputCount;
    private String inhibitGet;
    private String inhibitPut;
    private String queueType;
    private String queueDesc;
    private int backoutThreshold;
    private String backoutRequeueName;
    /** 最老消息已存在时间（秒），队列为空时为 -1 */
    private long oldestMessageAge;
    /** 最老消息的 put 时间（UTC, yyyy-MM-dd HH:mm:ss），无消息时为 null */
    private String oldestMessagePutTime;
    /** 最新消息的 put 时间（UTC, yyyy-MM-dd HH:mm:ss），无消息或超阈值时为 null */
    private String newestMessagePutTime;
    /** 累计入队（put）消息总数 */
    private long totalEnqueueCount;
    /** 累计出队（get）消息总数 */
    private long totalDequeueCount;
    /** 非正常时填充错误信息，正常时为 null */
    private String error;
}
