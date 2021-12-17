package cn.hdj.repeatsubmit.service.impl;

import cn.hdj.repeatsubmit.service.TokenService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2021/12/15 19:20
 */
@Service
public class TokenServiceImpl implements TokenService {


    public static final String TOKEN_PREFIX = "token_idempotent";
    public static final String TOKEN_HEADER_NAME = "x-token";
    public static final Long TOKEN_EXPIRE_TIME = 5 * 60L;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public String createToken() {
        String ID = UUID.randomUUID().toString();
        RBucket<String> bucket = this.redissonClient.<String>getBucket(String.format("%s:%s", TOKEN_PREFIX, ID), StringCodec.INSTANCE);
        //默认超时5分钟
        bucket.trySet(ID, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        return ID;
    }

    @Override
    public boolean checkToken(HttpServletRequest request) {
        // 从请求头中获取token信息
        String token = request.getHeader(TOKEN_HEADER_NAME);
        if (!StringUtils.hasLength(token)) {
            //从参数中获取token值
            token = request.getParameter(TOKEN_HEADER_NAME);
        }
        if (!StringUtils.hasLength(token)) {
            throw new DuplicateKeyException("重复提交，提交失败");
        }

        // 不为空则校验token信息
        RBucket<String> bucket = this.redissonClient.getBucket(String.format("%s:%s", TOKEN_PREFIX, token), StringCodec.INSTANCE);

        //获取，并删除
        String ID = bucket.getAndDelete();
        //不存在，则重复提交
        if (ID == null) {
            throw new DuplicateKeyException("重复提交，提交失败");
        }
        return true;
    }

}
