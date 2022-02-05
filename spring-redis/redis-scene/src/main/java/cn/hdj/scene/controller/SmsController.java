package cn.hdj.scene.controller;

import cn.hutool.core.date.DateUtil;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description: redis 限流器使用
 * @Author huangjiajian
 * @Date 2022/1/30 下午6:38
 * @see <a>https://www.yuque.com/h_dj/kd0g9f/pefeqn</a>
 */
@RestController
@RequestMapping(value = "/sms")
public class SmsController {

    @Autowired
    private RedissonClient redissonClient;


    @GetMapping("/send")
    public void  send(String mobile) throws InterruptedException {
        //初始化限流器
        RRateLimiter rateLimiter = this.redissonClient.getRateLimiter("limit:sms:"+mobile);
        //初始化设置频率
        //30秒内颁发一个令牌
        rateLimiter.trySetRate(RateType.OVERALL,1,30, RateIntervalUnit.SECONDS);

        if(rateLimiter.tryAcquire()){
            System.out.println("发送短信"+ DateUtil.formatDateTime(new Date())+" "+mobile);
            TimeUnit.SECONDS.sleep(5);
        }
        System.err.println("正在发送短信，不要重复发送！");
    }


}
