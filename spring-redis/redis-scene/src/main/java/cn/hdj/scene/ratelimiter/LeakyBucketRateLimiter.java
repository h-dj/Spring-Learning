package cn.hdj.scene.ratelimiter;

import java.util.concurrent.atomic.AtomicLong;

public class LeakyBucketRateLimiter {
    private final long capacity;    // 桶的容量
    private final long leakRate;        // 漏桶出水速率
    private final AtomicLong water;             // 当前桶中的水量
    private long lastLeakTime; // 上次漏水时间戳

    public LeakyBucketRateLimiter(long capacity, long leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;
        this.water = new AtomicLong(0);
        this.lastLeakTime = System.currentTimeMillis();
    }

    public synchronized boolean tryAcquire() {
        //获取当前时间
        long now = System.currentTimeMillis();
        // 计算漏掉的水滴数量
        long elapsedTime = now - lastLeakTime;
        long leaked = (elapsedTime * leakRate) / 1000;

        //更新水量
        water.set(Math.max(0, water.get() - leaked));
        lastLeakTime = now;

        if (water.get() < capacity) {
            water.incrementAndGet();
            return true;
        }
        return false;
    }


}