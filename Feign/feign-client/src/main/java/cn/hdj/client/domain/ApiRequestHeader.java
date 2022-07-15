package cn.hdj.client.domain;

import lombok.Data;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 10:08
 */
@Data
public class ApiRequestHeader {


    /**
     * channelId string 必须 技术渠道号: APP，H5，PC，微信公众号，微信小程序等，用来标识业务技术载体
     * requestTime string 必须 请求时间，格式：yyyyMMddHHmmss
     * serviceId string 必须 交易服务码，由服务提供方定义
     * serviceSn string 必须 交易流水号，请求方生成，建议使用 32位UUID
     * sid string 必须 全局请求流水， 由最外层的接入系统生成，要求全链路透传；推荐使用32位uuid。
     * accessToken string 非必须 请求Token，前端用来控制请求的合法性
     * businessChannel string 必须业务渠道号，合作机构，合作渠道，用来标识外部业务合作主体，由服务提供方定义
     * marketId string 非必须 营销渠道号，当一个业务渠道下面的某个技术渠道有多个入口时候，用于标识不同的入口
     * versionId string 必须 版本号
     */
    private String channelId;
    private String requestTime;
    private String serviceId;
    private String serviceSn;
    private String sid;
    private String accessToken;
    private String businessChannel;
    private String marketId;
    private String versionId;
}
