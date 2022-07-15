package cn.hdj.client.config;

import cn.hdj.client.domain.ApiRequest;
import cn.hdj.client.exception.ApiException;
import cn.hdj.client.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 13:38
 */
@ControllerAdvice
@Slf4j
public class CustomRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Autowired
    SecureProperties secureProperties;


    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {


        if (body instanceof ApiRequest) {
            ApiRequest apiRequest = (ApiRequest) body;

            //验签
            String message = SecurityUtil.getSignSrcSkipNull(apiRequest);
            boolean verify = SecurityUtil.verify(message, apiRequest.getSign(), secureProperties.getClient().getPublicKey());
            if (!verify) {
                throw new ApiException("验签失败!", 500);
            }

            //解密
            try {
                String requestBody = SecurityUtil.decryptInBase64OutString(apiRequest.getRequestEncrypted(), secureProperties.getServer().getPrivateKey());
                apiRequest.setRequest(requestBody);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new ApiException("解密失败!", 500);
            }
            return apiRequest;
        }


        return body;

    }

}
