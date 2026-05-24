package cn.reid.springbatchdemo.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

class ChunkMonitorLogger {

    private static final Logger log = LoggerFactory.getLogger("ChunkMonitorLogger");

    private final ObjectMapper objectMapper;

    ChunkMonitorLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void logChunk(String jobName, String stepName, int chunkIndex,
                  int itemCount, long readDurationNs, long processDurationNs,
                  long writeDurationNs, long readCount, long writeCount,
                  long filterCount, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("eventType", "CHUNK_COMPLETION");
            m.put("timestamp", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            m.put("jobName", jobName);
            m.put("stepName", stepName);
            m.put("chunkIndex", chunkIndex);
            m.put("itemCount", itemCount);
            m.put("readDurationMs", readDurationNs / 1_000_000);
            m.put("processDurationMs", processDurationNs / 1_000_000);
            m.put("writeDurationMs", writeDurationNs / 1_000_000);
            m.put("chunkTotalDurationMs", (readDurationNs + processDurationNs + writeDurationNs) / 1_000_000);
            m.put("readCount", readCount);
            m.put("writeCount", writeCount);
            m.put("filterCount", filterCount);
            m.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            m.put("endTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            log.info(objectMapper.writeValueAsString(m));
        } catch (JsonProcessingException e) {
            log.error("序列化chunk监控指标失败", e);
        }
    }
}
