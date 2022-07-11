package cn.hdj.server.domain;

import lombok.Data;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 10:08
 */
@Data
public class ApiRequest {

    /**
     * 请求头参数
     */
    private ApiRequestHeader header;
    /**
     * 未加密请求
     */
    private Object request;
    /**
     * 加密串
     */
    private String responseEncrypted;
    /**
     * 签名
     */
    private String sign;
}
