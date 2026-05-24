package cn.reid.springbatchdemo.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

class MonitorLogger {

    private static final Logger log = LoggerFactory.getLogger("MonitorLogger");

    private final ObjectMapper objectMapper;

    MonitorLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void logMetrics(StepExecution stepExecution, long startTime, long duration,
                    String fileType, String filePath) {
        try {
            Map<String, Object> metrics = new LinkedHashMap<>();
            metrics.put("timestamp",
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.ofEpochMilli(startTime)));
            metrics.put("eventType", "STEP_COMPLETION");
            metrics.put("jobName", stepExecution.getJobExecution().getJobInstance().getJobName());
            metrics.put("stepName", stepExecution.getStepName());

            long execId = stepExecution.getId();
            metrics.put("stepExecutionId", execId);
            metrics.put("exitStatus", stepExecution.getExitStatus().getExitCode());

            metrics.put("fileType", fileType);
            metrics.put("filePath", filePath);

            metrics.put("durationMs", duration);
            metrics.put("durationSeconds", duration / 1000);

            long readCount = stepExecution.getReadCount();
            long writeCount = stepExecution.getWriteCount();
            long skipCount = stepExecution.getSkipCount();
            long filterCount = readCount - writeCount - skipCount;

            metrics.put("readCount", readCount);
            metrics.put("writeCount", writeCount);
            metrics.put("filterCount", filterCount);
            metrics.put("skipCount", skipCount);
            metrics.put("processSkipCount", stepExecution.getProcessSkipCount());
            metrics.put("commitCount", stepExecution.getCommitCount());
            metrics.put("rollbackCount", stepExecution.getRollbackCount());

            if (stepExecution.getStartTime() != null) {
                metrics.put("startTime", stepExecution.getStartTime().toString());
            }
            if (stepExecution.getEndTime() != null) {
                metrics.put("endTime", stepExecution.getEndTime().toString());
            }

            log.info(objectMapper.writeValueAsString(metrics));
        } catch (JsonProcessingException e) {
            log.error("序列化监控指标失败", e);
        }
    }

    void logSkipSummary(String jobName, String stepName, int totalSkips, Map<String, Integer> skipReasons) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("eventType", "SKIP_SUMMARY");
            m.put("timestamp", DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(Instant.now()));
            m.put("jobName", jobName);
            m.put("stepName", stepName);
            m.put("totalSkips", totalSkips);
            m.put("skipReasons", skipReasons);
            log.info(objectMapper.writeValueAsString(m));
        } catch (JsonProcessingException e) {
            log.error("序列化skip汇总失败", e);
        }
    }
}
