package cn.hdj.scene.ratelimiter;

import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiter {

    /**
     * 最大token数量
     */
    private final long maxTokens;
    /**
     * 填充token数量
     */
    private final long refillTokens;
    /**
     * 填充token 时间间隔
     */
    private final long refillIntervalInMillis;
    /**
     * 当前可用token
     */
    private final AtomicLong availableTokens;
    /**
     * 上一次填充token 时间
     */
    private volatile long lastRefillTime;


    public TokenBucketRateLimiter(long maxTokens, long refillTokens, long refillIntervalInMillis) {
        this.maxTokens = maxTokens;
        this.refillTokens = refillTokens;
        this.refillIntervalInMillis = refillIntervalInMillis;
        this.availableTokens = new AtomicLong(maxTokens);
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean tryAcquire() {
        //是否需要填充token
        refillTokensIfNecessary();

        //如果当前token 数大于请求数量
        if (this.availableTokens.get() > 0) {
            this.availableTokens.decrementAndGet();
            return true;
        }
        return false;
    }

    private void refillTokensIfNecessary() {
        //获取当前时间
        long now = System.currentTimeMillis();
        //判断是否需要填充，时间间隔是否在填充时间段内
        if ((now - lastRefillTime) >= refillIntervalInMillis) {
            //计算token 填充数量， （当前时间段/填充时间间隔） * 填充时间间隔内填充的token 数量
            long tokensToAdd = ((now - lastRefillTime) / refillIntervalInMillis) * refillTokens;
            //更新可用token 数量
            long newTokenCount = Math.min(maxTokens, availableTokens.get() + tokensToAdd);
            availableTokens.set(newTokenCount);
            lastRefillTime = now;
        }
    }
}
