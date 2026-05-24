package cn.reid.springbatchdemo.monitor;

import cn.reid.springbatchdemo.entity.Student;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ItemTimingListener implements
        ItemReadListener<Student>,
        ItemProcessListener<Student, Student>,
        ItemWriteListener<Student> {

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
    public void afterRead(Student item) {
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
    public void beforeProcess(Student item) {
        processStart.set(System.nanoTime());
    }

    @Override
    public void afterProcess(Student item, Student result) {
        Long start = processStart.get();
        processStart.remove();
        if (start != null) {
            processTotalNs.addAndGet(System.nanoTime() - start);
        }
    }

    @Override
    public void onProcessError(Student item, Exception ex) {
        processStart.remove();
    }

    // ========== ItemWriteListener ==========

    @Override
    public void beforeWrite(Chunk<? extends Student> items) {
        writeStart.set(System.nanoTime());
    }

    @Override
    public void afterWrite(Chunk<? extends Student> items) {
        Long start = writeStart.get();
        writeStart.remove();
        if (start != null) {
            writeTotalNs.addAndGet(System.nanoTime() - start);
        }
    }

    @Override
    public void onWriteError(Exception ex, Chunk<? extends Student> items) {
        writeStart.remove();
    }
}
