package cn.reid.springbatchdemo.monitor;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ItemTimingListener implements
        ItemReadListener<Object>,
        ItemProcessListener<Object, Object>,
        ItemWriteListener<Object> {

    private final ThreadLocal<Long> readStart = new ThreadLocal<>();
    private final ThreadLocal<Long> processStart = new ThreadLocal<>();
    private final ThreadLocal<Long> writeStart = new ThreadLocal<>();

    final AtomicLong readTotalNs = new AtomicLong();
    final AtomicLong processTotalNs = new AtomicLong();
    final AtomicLong writeTotalNs = new AtomicLong();

    void reset() {
        readTotalNs.set(0);
        processTotalNs.set(0);
        writeTotalNs.set(0);
    }

    // ========== ItemReadListener ==========

    @Override
    public void beforeRead() {
        readStart.set(System.nanoTime());
    }

    @Override
    public void afterRead(Object item) {
        Long start = readStart.get();
        readStart.remove();
        if (start != null) {
            readTotalNs.addAndGet(System.nanoTime() - start);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        readStart.remove();
    }

    // ========== ItemProcessListener ==========

    @Override
    public void beforeProcess(Object item) {
        processStart.set(System.nanoTime());
    }

    @Override
    public void afterProcess(Object item, Object result) {
        Long start = processStart.get();
        processStart.remove();
        if (start != null) {
            processTotalNs.addAndGet(System.nanoTime() - start);
        }
    }

    @Override
    public void onProcessError(Object item, Exception ex) {
        processStart.remove();
    }

    // ========== ItemWriteListener ==========

    @Override
    public void beforeWrite(Chunk<?> items) {
        writeStart.set(System.nanoTime());
    }

    @Override
    public void afterWrite(Chunk<?> items) {
        Long start = writeStart.get();
        writeStart.remove();
        if (start != null) {
            writeTotalNs.addAndGet(System.nanoTime() - start);
        }
    }

    @Override
    public void onWriteError(Exception ex, Chunk<?> items) {
        writeStart.remove();
    }
}
