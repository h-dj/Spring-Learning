package cn.hdj.scene.ratelimiter;

import java.util.LinkedList;
import java.util.Queue;

public class SlidingWindowRateLimiter {

    /**
     * 窗口内最大请求
     */
    private final int maxRequests;
    /**
     * 窗口区间
     */
    private final long windowSizeInMillis;
    /**
     * 滑动窗口
     */
    private final Queue<Long> widonwQueue;

    public SlidingWindowRateLimiter(int maxRequests, long windowSizeInMillis) {
        this.maxRequests = maxRequests;
        this.windowSizeInMillis = windowSizeInMillis;
        this.widonwQueue = new LinkedList<>();
    }

    /**
     * 限流
     *
     */
    public synchronized boolean tryAcquire() {
        //获取当前时间
        long now = System.currentTimeMillis();

        // 移除时间窗口之外的请求
        while (!widonwQueue.isEmpty() && (now - widonwQueue.peek()) > windowSizeInMillis) {
            widonwQueue.poll();
        }

        // 如果请求数在窗口内没有超限，则允许请求
        if (widonwQueue.size() < maxRequests) {
            widonwQueue.offer(now);
            return true;
        }

        // // 超过请求限制，拒绝请求
        return false;
    }
}
