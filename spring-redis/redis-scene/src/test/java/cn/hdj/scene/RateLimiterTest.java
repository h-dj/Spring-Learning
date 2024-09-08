package cn.hdj.scene;

import cn.hdj.scene.ratelimiter.FixedWindowRateLimiter;
import cn.hdj.scene.ratelimiter.LeakyBucketRateLimiter;
import cn.hdj.scene.ratelimiter.SlidingWindowRateLimiter;
import cn.hdj.scene.ratelimiter.TokenBucketRateLimiter;
import org.redisson.Redisson;
import org.redisson.api.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RateLimiterTest {

    public static void main(String[] args) {
        FixedWindowRateLimiter fixedWindowRateLimiter = new FixedWindowRateLimiter(1, 1000);
        SlidingWindowRateLimiter slidingWindowRateLimiter = new SlidingWindowRateLimiter(1, 1000);

        TokenBucketRateLimiter tokenBucketRateLimiter=new TokenBucketRateLimiter(1,1,1000);
        LeakyBucketRateLimiter leakyBucketRateLimiter=new LeakyBucketRateLimiter(1,1);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executorService.execute(() -> {
                boolean tryAcquire = leakyBucketRateLimiter.tryAcquire();
                if (tryAcquire) {
                    System.out.println("执行"+ finalI);
                } else {
                    System.out.println("限流"+ finalI);
                }
            });
        }

        executorService.shutdown();


        RedissonClient redissonClient=Redisson.create();

        RRateLimiter skc = redissonClient.getRateLimiter("skc");

        skc.setRateAsync(RateType.OVERALL,10,1, RateIntervalUnit.SECONDS);

        skc.tryAcquireAsync(1);
    }
}
