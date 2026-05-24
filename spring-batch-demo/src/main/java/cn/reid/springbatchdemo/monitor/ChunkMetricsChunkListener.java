package cn.reid.springbatchdemo.monitor;

import cn.reid.springbatchdemo.listener.FileProcessingMetricsListener;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.time.LocalDateTime;

public class ChunkMetricsChunkListener implements ChunkListener {

    private final ItemTimingListener timingListener;
    private final SkipCollectorListener skipListener;
    private final FileProcessingMetricsListener stepListener;
    private final MonitoringFacade monitoringFacade;
    private int chunkIndex = 0;
    private LocalDateTime chunkStart;

    public ChunkMetricsChunkListener(ItemTimingListener timingListener,
                                      SkipCollectorListener skipListener,
                                      FileProcessingMetricsListener stepListener,
                                      MonitoringFacade monitoringFacade) {
        this.timingListener = timingListener;
        this.skipListener = skipListener;
        this.stepListener = stepListener;
        this.monitoringFacade = monitoringFacade;
    }

    @Override
    public void beforeChunk(ChunkContext context) {
        chunkStart = LocalDateTime.now();
        timingListener.reset();
        chunkIndex++;
    }

    @Override
    public void afterChunk(ChunkContext context) {
        LocalDateTime chunkEnd = LocalDateTime.now();
        var stepCtx = context.getStepContext().getStepExecution();
        String jobName = stepCtx.getJobExecution().getJobInstance().getJobName();
        String stepName = stepCtx.getStepName();
        String fileType = stepCtx.getJobParameters().getString("fileType");
        int itemCount = (int) (stepCtx.getReadCount() - stepCtx.getRollbackCount());

        monitoringFacade.logChunkMetrics(
                jobName, stepName, chunkIndex,
                itemCount,
                timingListener.readTotalNs.get(),
                timingListener.processTotalNs.get(),
                timingListener.writeTotalNs.get(),
                stepCtx.getReadCount(),
                stepCtx.getWriteCount(),
                Math.max(0, stepCtx.getReadCount() - stepCtx.getWriteCount() - stepCtx.getSkipCount()),
                fileType,
                chunkStart, chunkEnd
        );
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        // 出错时不输出chunk指标
    }
}
