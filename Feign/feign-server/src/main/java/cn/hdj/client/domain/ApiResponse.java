package cn.hdj.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 10:08
 */
@Data
public class ApiResponse {
    /**
     * 响应头参数
     */
    private ApiResponseHeader header;
    /**
     * 未加密响应
     */
    @JsonIgnore
    private Object response;
    /**
     * 加密串
     */
    private String responseEncrypted;
    /**
     * 签名
     */
    private String sign;

    public ApiResponse() {
    }

    public ApiResponse(Object response) {
        this.header = new ApiResponseHeader();
        this.response = response;
    }
}
