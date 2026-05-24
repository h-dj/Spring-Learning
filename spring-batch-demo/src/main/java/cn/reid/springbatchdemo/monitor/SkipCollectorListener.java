package cn.reid.springbatchdemo.monitor;

import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SkipCollectorListener implements SkipListener<Object, Object> {

    private final ConcurrentHashMap<String, AtomicInteger> skipReasons = new ConcurrentHashMap<>();
    private volatile boolean enabled = false;

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void reset() {
        skipReasons.clear();
    }

    public Map<String, Integer> getSummary() {
        Map<String, Integer> result = new LinkedHashMap<>();
        skipReasons.forEach((reason, count) -> result.put(reason, count.get()));
        return result;
    }

    public int getTotalSkips() {
        return skipReasons.values().stream().mapToInt(AtomicInteger::get).sum();
    }

    private void record(String reason) {
        if (enabled) {
            skipReasons.computeIfAbsent(reason, k -> new AtomicInteger()).incrementAndGet();
        }
    }

    @Override
    public void onSkipInRead(Throwable t) {
        record(formatReason(t));
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        record(formatReason(t));
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        record(formatReason(t));
    }

    private static String formatReason(Throwable t) {
        String msg = t.getMessage();
        return t.getClass().getSimpleName() + ": " + (msg != null ? msg : "");
    }
}
