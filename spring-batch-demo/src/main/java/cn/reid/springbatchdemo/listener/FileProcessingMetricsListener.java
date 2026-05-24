package cn.reid.springbatchdemo.listener;

import cn.reid.springbatchdemo.monitor.MonitorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FileProcessingMetricsListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(FileProcessingMetricsListener.class);

    private final MonitorLogger monitorLogger;
    private long startTime;

    public FileProcessingMetricsListener(MonitorLogger monitorLogger) {
        this.monitorLogger = monitorLogger;
    }

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

        String fileType = stepExecution.getJobParameters().getString("fileType");
        String filePath = stepExecution.getJobParameters().getString("filePath");

        monitorLogger.logMetrics(stepExecution, startTime, duration, fileType, filePath);

        return stepExecution.getExitStatus();
    }
}
