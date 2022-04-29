package cn.hdj.message.message.domain;

import cn.hdj.message.message.enums.ChannelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/2/25 10:44
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseMessageRequest implements MessageRequest {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 变量
     */
    private Map<String, Object> varMap;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 发送渠道
     */
    private ChannelTypeEnum channelTypeEnum;

    /**
     * 消息业务类型编码
     */
    private String messageTypeCode;
}
