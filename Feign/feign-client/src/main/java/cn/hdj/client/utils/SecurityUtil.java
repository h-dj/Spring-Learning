package cn.hdj.client.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class SecurityUtil {

    /**
     * 加密
     *
     * @param message
     * @return
     */
    public static String encryptInStringOutBase64(String message, String publicKeyStr) {
        return SecureUtil.rsa(null, publicKeyStr).encryptBase64(message, KeyType.PublicKey);
    }


    /**
     * 解密
     *
     * @param messageBase64
     * @param privateKeyStr
     * @return
     */
    public static String decryptInBase64OutString(String messageBase64, String privateKeyStr) {
        return SecureUtil.rsa(privateKeyStr, null).decryptStr(messageBase64, KeyType.PrivateKey, CharsetUtil.CHARSET_UTF_8);
    }


    /**
     * 验签
     *
     * @return
     */
    public static boolean verify(String signStr , String sign, String publicKeyStr) {
        if (sign == null || signStr == null || publicKeyStr == null) {
            return false;
        }
        try {
            Sign signObj = SecureUtil.sign(SignAlgorithm.SHA1withRSA, null, publicKeyStr);
            //验证签名
            return signObj.verify(StrUtil.bytes(signStr, CharsetUtil.CHARSET_UTF_8), Base64.decodeBase64(sign));
        } catch (Exception ex) {
            log.warn("verify [{}] sign [{}] , public key [{}] exception:"
                    , signStr
                    , sign
                    , publicKeyStr
                    , ex);
            return false;
        }

    }

    /**
     * 签名
     *
     * @param message
     * @param privateKeyStr
     * @return
     */
    public static String sign(String message, String privateKeyStr) {
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA1withRSA, privateKeyStr, null);
        //签名
        return Base64.encodeBase64String(sign.sign(message));
    }

    /**
     * 获取签名拼接
     *
     * @param o
     * @param skipFields
     * @return
     */
    public static String getSignSrcSkipNull(Object o, String... skipFields) {
        final Map<String, Object> map = BeanUtil.beanToMap(o, new TreeMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldNameEditor(s -> StrUtil.toUnderlineCase(s))
                        .setFieldValueEditor((key, value) -> value == null || ClassUtil.isBasicType(value.getClass()) ? value : JSONUtil.toJsonStr(value))

        );
        map.remove("sign");
        CollectionUtil.toList(skipFields)
                .forEach(v -> map.remove(v));
        return UrlQuery.of(map)
                .build(null, false);
    }
}
