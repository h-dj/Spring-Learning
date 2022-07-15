package cn.hdj.client.feign;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/14 17:29
 */

import cn.hdj.client.config.CustomFeignConfiguration;
import cn.hdj.client.domain.ApiRequest;
import cn.hdj.client.domain.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用用户中心的Feign客户端代理类
 */
@FeignClient(name = "user-center",url = "${feign.base-url}",configuration = CustomFeignConfiguration.class,fallback = UserCenterFeignClientFallback.class)
public interface UserCenterFeignClient {

    @PostMapping(value = "/users/list")
    ApiResponse list(@RequestBody ApiRequest apiRequest);
}
