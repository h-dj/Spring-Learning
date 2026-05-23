package cn.reid.springbatchdemo.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FileProcessingMetricsListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(FileProcessingMetricsListener.class);

    private long startTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.startTime = System.currentTimeMillis();

        String fileType = stepExecution.getJobParameters().getString("fileType");
        String filePath = stepExecution.getJobParameters().getString("filePath");

        log.info("===== 开始处理文件: type={}, path={} =====", fileType, filePath);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long duration = System.currentTimeMillis() - startTime;

        long readCount = stepExecution.getReadCount();
        long writeCount = stepExecution.getWriteCount();
        long skipCount = stepExecution.getSkipCount();
        long filterCount = readCount - writeCount - skipCount;

        log.info("===== 文件处理完成 | fileType={} | 耗时={}秒 | 读取={} | 写入={} | 过滤={} | 跳过={} =====",
                stepExecution.getJobParameters().getString("fileType"),
                duration / 1000,
                readCount,
                writeCount,
                filterCount,
                skipCount
        );

        return stepExecution.getExitStatus();
    }
}
