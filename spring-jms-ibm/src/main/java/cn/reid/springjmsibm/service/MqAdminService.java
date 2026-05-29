package cn.reid.springjmsibm.service;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqAdminService {

    private final MQQueueManager mqQueueManager;

    /**
     * 获取指定队列的深度（当前消息数）
     */
    public int getQueueDepth(String queueName) {
        try {
            int openOptions = CMQC.MQOO_INQUIRE;
            MQQueue queue = mqQueueManager.accessQueue(queueName, openOptions, null, null, null);
            int depth = queue.getCurrentDepth();
            queue.close();
            log.info("Queue [{}] depth: {}", queueName, depth);
            return depth;
        } catch (MQException e) {
            log.error("Failed to get queue depth for [{}]", queueName, e);
            throw new RuntimeException("Failed to get queue depth: " + e.getLocalizedMessage(), e);
        }
    }
}
