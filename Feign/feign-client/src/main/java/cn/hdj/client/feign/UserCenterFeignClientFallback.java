package cn.hdj.client.feign;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/14 17:29
 */

import cn.hdj.client.domain.ApiRequest;
import cn.hdj.client.domain.ApiResponse;
import org.springframework.stereotype.Component;

/**
 * 调用用户中心的Feign客户端代理类
 */
@Component
public class UserCenterFeignClientFallback implements UserCenterFeignClient{

    @Override
    public ApiResponse list(ApiRequest apiRequest) {
        throw new RuntimeException("UserCenterFeignClient 处理错误！");
    }
}
