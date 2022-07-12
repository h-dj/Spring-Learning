package cn.hdj.server.domain;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.Data;

import java.util.Date;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 10:08
 */
@Data
public class ApiResponseHeader {
    /**
     * code string 必须 响应应答码
     * desc string 必须 响应描述
     * serviceTime string 必须 响应时间
     * serviceSn string 必须 响应流水号，原值返回
     * sid string 必须 请求报文中的全局请求流水
     * businessChannel string 必须 业务渠道号，原值返回
     * serviceId string 必须 交易服务码，原值返回
     */
    private String code;
    private String serviceTime;
    private String sid;
    private String serviceId;
    private String serviceSn;
    private String desc;
    private String businessChannel;

    public ApiResponseHeader() {
        this.code = "200";
        this.desc = "ok";
        this.serviceTime = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT);
        this.serviceSn = IdUtil.fastSimpleUUID();
        this.sid = IdUtil.fastSimpleUUID();
        this.businessChannel = "test";
        this.serviceId = "sys.test";


    }
}
