package cn.hdj.server.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;

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
     * @param message
     * @param signStr
     * @param publicKeyStr
     * @return
     */
    public static boolean verify(String message, String signStr, String publicKeyStr) {
        if (message == null || signStr == null || publicKeyStr == null) {
            return false;
        }
        try {
            Sign sign = SecureUtil.sign(SignAlgorithm.MD5withRSA, null, publicKeyStr);
            //验证签名
            return sign.verify(StrUtil.bytes(message, CharsetUtil.CHARSET_UTF_8), StrUtil.bytes(signStr, CharsetUtil.CHARSET_UTF_8));
        } catch (Exception ex) {
            log.warn("verify [{}] sign [{}] , public key [{}] exception:"
                    , message
                    , signStr
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
        Sign sign = SecureUtil.sign(SignAlgorithm.MD5withRSA, privateKeyStr, null);
        //签名
        return Base64.encodeBase64String(sign.sign(message));
    }
}
