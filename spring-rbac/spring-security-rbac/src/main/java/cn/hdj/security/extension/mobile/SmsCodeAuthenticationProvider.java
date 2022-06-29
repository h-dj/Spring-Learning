package cn.hdj.security.extension.mobile;

import cn.hdj.common.api.ResponseCodeEnum;
import cn.hdj.common.consts.GlobalConstants;
import cn.hdj.common.exception.BaseException;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import sun.security.util.SecurityConstants;

import java.util.HashSet;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/6/28 14:53
 */
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    @Setter
    private UserDetailsService userDetailsService;
    @Setter
    private RedissonClient redissonClient;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;
        String mobile = (String) authenticationToken.getPrincipal();
        String code = (String) authenticationToken.getCredentials();

        //获取验证码
        String codeKey = GlobalConstants.SMS_CODE_PREFIX + mobile;
        //验证码只能使用一次

        String correctCode = StrUtil.str(redissonClient.getBucket(codeKey,new JsonJacksonCodec()).getAndDelete(), CharsetUtil.CHARSET_UTF_8);

        // 验证码比对
        if (StrUtil.isBlank(correctCode) || !StrUtil.equals(code, correctCode)) {
            throw new BaseException(ResponseCodeEnum.SMS_CAPTCHA_WRONG.getMsg(), ResponseCodeEnum.SMS_CAPTCHA_WRONG.getCode());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(mobile);
        SmsCodeAuthenticationToken result = new SmsCodeAuthenticationToken(userDetails, authentication.getCredentials(), userDetails.getAuthorities());
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
