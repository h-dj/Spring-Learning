package cn.hdj.client.feign;

import cn.hdj.client.domain.ApiRequest;
import cn.hdj.client.domain.ApiResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/14 18:03
 */
@SpringBootTest
@Slf4j
class UserCenterFeignClientTest  {

    @Autowired
    private UserCenterFeignClient userCenterFeignClient;

    @Test
    void list() {
        ApiResponse response = userCenterFeignClient.list(new ApiRequest());
        log.info(JSONUtil.toJsonPrettyStr(response));
    }
}