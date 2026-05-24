package cn.reid.springbatchdemo.listener;

import cn.reid.springbatchdemo.monitor.MonitoringFacade;
import cn.reid.springbatchdemo.monitor.SkipCollectorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FileProcessingMetricsListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(FileProcessingMetricsListener.class);

    private final MonitoringFacade monitoringFacade;
    private final SkipCollectorListener skipCollectorListener;
    private long startTime;

    public FileProcessingMetricsListener(MonitoringFacade monitoringFacade,
                                          SkipCollectorListener skipCollectorListener) {
        this.monitoringFacade = monitoringFacade;
        this.skipCollectorListener = skipCollectorListener;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.startTime = System.currentTimeMillis();
        skipCollectorListener.reset();
        skipCollectorListener.enable();

        String fileType = stepExecution.getJobParameters().getString("fileType");
        String filePath = stepExecution.getJobParameters().getString("filePath");

        log.info("===== 开始处理文件: type={}, path={} =====", fileType, filePath);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long duration = System.currentTimeMillis() - startTime;

        String fileType = stepExecution.getJobParameters().getString("fileType");
        String filePath = stepExecution.getJobParameters().getString("filePath");

        monitoringFacade.logStepMetrics(stepExecution, startTime, duration, fileType, filePath);

        // 输出跳过原因汇总
        skipCollectorListener.disable();
        int totalSkips = skipCollectorListener.getTotalSkips();
        if (totalSkips > 0) {
            String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
            monitoringFacade.logSkipSummary(
                    jobName, stepExecution.getStepName(),
                    totalSkips, skipCollectorListener.getSummary());
        }

        return stepExecution.getExitStatus();
    }
}
