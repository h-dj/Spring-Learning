package cn.hdj.server.config;

import cn.hdj.server.domain.ApiRequest;
import cn.hdj.server.exception.ApiException;
import cn.hdj.server.utils.SecurityUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.net.url.UrlQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.Map;

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
            Map<String, Object> map = BeanUtil.beanToMap(apiRequest, true, true);
            String message = UrlQuery.of(map)
                    .build(null, false);
            boolean verify = SecurityUtil.verify(message, apiRequest.getSign(), secureProperties.getReceiver().getPublicKey());
            if (!verify) {
                throw new ApiException("验签失败!", 500);
            }

            //解密
            try {
                String requestBody = SecurityUtil.decryptInBase64OutString(apiRequest.getRequestEncrypted(), secureProperties.getSender().getPrivateKey());
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
