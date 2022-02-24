package cn.hdj.message.message.domain.email;

import cn.hdj.message.message.domain.MessageRequest;
import cn.hdj.message.message.enums.ChannelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2022/2/23 14:05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessageRequest implements java.io.Serializable {

    /**
     * 发件人
     */
    private String from;
    /**
     * 收件人
     */
    private List<String> toList;
    /**
     * 变量
     */
    private Map<String, Object> varMap;

    /**
     * 模板ID
     */
    private String templateId;


}
