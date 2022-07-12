package cn.hdj.server.config;

import cn.hdj.server.domain.ApiResponse;
import cn.hdj.server.utils.SecurityUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

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
            String encrypt = SecurityUtil.encryptInStringOutBase64(jsonStr, secureProperties.getReceiver().getPublicKey());
            apiResponse.setResponseEncrypted(encrypt);
            apiResponse.setResponse(null);

            //签名
            Map<String, Object> map = BeanUtil.beanToMap(apiResponse, true, true);

            String message = UrlQuery.of(map)
                    .build(null, false);
            String sign = SecurityUtil.sign(message, secureProperties.getSender().getPrivateKey());

            apiResponse.setSign(sign);

            return apiResponse;
        }

        return body;
    }
}
