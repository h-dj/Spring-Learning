package cn.hdj.client.config;

import cn.hdj.client.domain.ApiResponse;
import cn.hdj.client.utils.SecurityUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 13:38
 */
@ControllerAdvice
public class CustomResponseBodyAdvice implements ResponseBodyAdvice {

    @Autowired
    SecureProperties secureProperties;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {


        if (body instanceof ApiResponse) {
            ApiResponse apiResponse = (ApiResponse) body;
            String jsonStr = JSONUtil.toJsonStr(apiResponse.getResponse());
            jsonStr = StrUtil.emptyToDefault(jsonStr, StrUtil.EMPTY);

            //加密
            String encrypt = SecurityUtil.encryptInStringOutBase64(jsonStr, secureProperties.getClient().getPublicKey());
            apiResponse.setResponseEncrypted(encrypt);
            apiResponse.setResponse(null);

            //签名
            String signSrc = SecurityUtil.getSignSrcSkipNull(apiResponse);
            String sign = SecurityUtil.sign(signSrc,secureProperties.getServer().getPrivateKey());
            apiResponse.setSign(sign);
            return apiResponse;
        }

        return body;
    }
}
