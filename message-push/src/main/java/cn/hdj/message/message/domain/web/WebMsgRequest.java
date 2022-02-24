package cn.hdj.message.message.domain.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2022/2/23 14:05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebMsgRequest implements java.io.Serializable {

    /**
     * 发送人
     */
    private Integer sendUserId;
    /**
     * 接收人
     */
    private List<Integer> receiveUserIds;
    /**
     * 模板id
     */
    private String templateId;

    /**
     * 模板参数
     */
    private Map<String, Object> varMap;


}
