package cn.hdj.client.config;

import cn.hdj.client.domain.ApiResponse;
import cn.hdj.client.utils.SecurityUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringDecoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/15 15:07
 */
@Slf4j
public class FeignResponseDecoder extends SpringDecoder {


    public FeignResponseDecoder(ObjectFactory<HttpMessageConverters> messageConverters, ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        super(messageConverters, customizers);
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        Object result = super.decode(response, type);
        if (ApiResponse.class.isAssignableFrom(TypeUtil.getClass(type))) {
            SecureProperties secureProperties = SpringUtil.getBean(SecureProperties.class);
            ApiResponse apiResponse = (ApiResponse) result;

            //验签
            String message = SecurityUtil.getSignSrcSkipNull(apiResponse);
            boolean verify = SecurityUtil.verify(message, apiResponse.getSign(), secureProperties.getServer().getPublicKey());
            if (!verify) {
                throw new DecodeException(response.status(), "验签失败.", response.request());
            }
            //解密
            try {
                String responseBody = SecurityUtil.decryptInBase64OutString(apiResponse.getResponseEncrypted(), secureProperties.getClient().getPrivateKey());
                apiResponse.setResponse(JSONUtil.toBean(responseBody, LinkedHashMap.class));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new DecodeException(response.status(), "解密失败", response.request());
            }
        }
        return result;
    }
}
