package cn.hdj.message.message.domain;

import cn.hdj.message.message.enums.ChannelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
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
public class DeduplicationMessageRequest{

    /**
     * 去重唯一key
     */
    private String deduplicateKey;
    /**
     * 超时时间
     */
    private Long timeout;
    /**
     * 限制次数
     */
    private Long count;

}
