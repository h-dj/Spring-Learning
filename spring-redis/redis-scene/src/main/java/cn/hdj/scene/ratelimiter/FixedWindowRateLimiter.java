package cn.hdj.scene.ratelimiter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 固定窗口算法
 */
public class FixedWindowRateLimiter {
    /**
     * 最多请求
     */
    private final int maxRequests;
    /**
     * 时间窗口
     */
    private final long windowDurationInMillis;
    /**
     * 窗口开始时间
     */
    private long windowStartTime;
    /**
     * 计数器
     */
    private final AtomicInteger requestCount;

    public FixedWindowRateLimiter(int maxRequests, long windowDurationInMillis) {
        this.maxRequests = maxRequests;
        this.windowDurationInMillis = windowDurationInMillis;
        this.windowStartTime = System.currentTimeMillis();
        this.requestCount = new AtomicInteger(0);
    }

    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();

        // 如果当前时间超过窗口时间，则重置窗口
        if (now - windowStartTime > windowDurationInMillis) {
            windowStartTime = now;
            requestCount.set(0);
        }

        // 如果请求数还在限制内，则允许请求
        if (requestCount.incrementAndGet() <= maxRequests) {
            return true;
        }

        // 超过请求限制，拒绝请求
        return false;
    }
}