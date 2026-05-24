package cn.reid.springbatchdemo.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class MonitoringFacade {

    private final MonitorLogger monitorLogger;
    private final ChunkMonitorLogger chunkMonitorLogger;

    MonitoringFacade(ObjectMapper objectMapper) {
        this.monitorLogger = new MonitorLogger(objectMapper);
        this.chunkMonitorLogger = new ChunkMonitorLogger(objectMapper);
    }

    public void logStepMetrics(StepExecution stepExecution, long startTime,
                                long duration, String fileType, String filePath) {
        monitorLogger.logMetrics(stepExecution, startTime, duration, fileType, filePath);
    }

    public void logSkipSummary(String jobName, String stepName,
                                int totalSkips, Map<String, Integer> skipReasons) {
        monitorLogger.logSkipSummary(jobName, stepName, totalSkips, skipReasons);
    }

    public void logChunkMetrics(String jobName, String stepName, int chunkIndex,
                                 int itemCount, long readDurationNs, long processDurationNs,
                                 long writeDurationNs, long readCount, long writeCount,
                                 long filterCount, String fileType,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        chunkMonitorLogger.logChunk(jobName, stepName, chunkIndex, itemCount,
                readDurationNs, processDurationNs, writeDurationNs,
                readCount, writeCount, filterCount, fileType, startTime, endTime);
    }
}
