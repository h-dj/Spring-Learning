package cn.hdj.modules.api.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/8/5 17:46
 */
@Data
public class MessageSendRequest {

    /**
     * 接收人
     */
    private List<Receiver> receiverAddressList;
    //消息类型编码
    private String messageTypeCode;
    //发送参数
    private Map<String, String> args;
    //是否批量发送
    private boolean batchSend;
    // 抄送
    private List<String> ccList;
    // 密送
    private List<String> bccList;
}
