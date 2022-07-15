package cn.hdj.client.config;

import cn.hdj.client.domain.ApiRequest;
import cn.hdj.client.utils.SecurityUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;

import java.lang.reflect.Type;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/15 15:07
 */
public class FeignRequestEncoder extends SpringEncoder {


    public FeignRequestEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        super(messageConverters);
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (object != null) {
            if (ApiRequest.class.isAssignableFrom(TypeUtil.getClass(bodyType))) {
                SecureProperties secureProperties = SpringUtil.getBean(SecureProperties.class);
                ApiRequest apiRequest = (ApiRequest) object;
                Object request = apiRequest.getRequest();
                String jsonStr = JSONUtil.toJsonStr(request);
                jsonStr = StrUtil.emptyToDefault(jsonStr, StrUtil.EMPTY);
                //加密
                String encrypt = SecurityUtil.encryptInStringOutBase64(jsonStr, secureProperties.getServer().getPublicKey());
                apiRequest.setRequestEncrypted(encrypt);
                apiRequest.setRequest(null);
                //签名
                String signSrc = SecurityUtil.getSignSrcSkipNull(apiRequest);
                String sign = SecurityUtil.sign(signSrc, secureProperties.getClient().getPrivateKey());
                apiRequest.setSign(sign);
            }
        }
        super.encode(object,bodyType,template);
    }
}
