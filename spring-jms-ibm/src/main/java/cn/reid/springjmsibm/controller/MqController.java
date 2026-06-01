package cn.reid.springjmsibm.controller;

import cn.reid.springjmsibm.dto.BrowseFilter;
import cn.reid.springjmsibm.dto.BrowseMessageDTO;
import cn.reid.springjmsibm.service.JmsBrowseService;
import cn.reid.springjmsibm.service.MqAdminService;
import cn.reid.springjmsibm.service.MqMessageService;
import cn.reid.springjmsibm.service.MqRequestReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mq")
@RequiredArgsConstructor
public class MqController {

    private final MqMessageService mqMessageService;
    private final MqAdminService mqAdminService;
    private final JmsBrowseService jmsBrowseService;
    private final MqRequestReplyService mqRequestReplyService;

    /**
     * 发送消息到 MQ
     * GET /mq/send?msg=hello
     *
     * http://127.0.0.1:8080/mq/send?msg=1111
     */
    @GetMapping("/send")
    public Map<String, Object> sendMessage(@RequestParam(defaultValue = "Hello IBM MQ") String msg) {
        mqMessageService.sendTextMessage(msg);
        return Map.of(
                "code", 200,
                "message", "Message sent",
                "data", msg
        );
    }

    /**
     * 发送消息到指定队列
     * GET /mq/send-to?queue=QUEUE.NAME&msg=hello
     *
     * http://127.0.0.1:8080/mq/send-to?queue=DEV.QUEUE.2&msg=hello
     */
    @GetMapping("/send-to")
    public Map<String, Object> sendToQueue(
            @RequestParam String queue,
            @RequestParam(defaultValue = "Hello IBM MQ") String msg) {
        mqMessageService.sendTextMessage(queue, msg);
        return Map.of(
                "code", 200,
                "message", "Message sent to " + queue,
                "data", msg
        );
    }

    /**
     * 接收消息（同步）
     * GET /mq/receive
     *
     * http://127.0.0.1:8080/mq/receive
     */
    @GetMapping("/receive")
    public Map<String, Object> receiveMessage() {
        String message = mqMessageService.receiveMessage(3000);
        if (message != null) {
            return Map.of(
                    "code", 200,
                    "message", "Message received",
                    "data", message
            );
        }
        return Map.of(
                "code", 204,
                "message", "No message available",
                "data", null
        );
    }

    /**
     * 获取队列深度
     * GET /mq/depth?queue=DEV.QUEUE.1
     *
     * http://127.0.0.1:8080/mq/depth?queue=DEV.QUEUE.1
     */
    @GetMapping("/depth")
    public Map<String, Object> queueDepth(@RequestParam(defaultValue = "DEV.QUEUE.1") String queue) {
        int depth = mqAdminService.getQueueDepth(queue);
        return Map.of(
                "code", 200,
                "queue", queue,
                "depth", depth
        );
    }

    /**
     * 使用 IBM MQ 原生 API 浏览队列消息（非破坏性）
     * GET /mq/browse?queueName=DEV.QUEUE.1&limit=20&offset=0
     *
     * http://127.0.0.1:8080/mq/browse?queueName=DEV.QUEUE.1&limit=10
     */
    @GetMapping("/browse")
    public Map<String, Object> browseMessages(
            @RequestParam String queueName,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String msgId,
            @RequestParam(required = false) String putUserId,
            @RequestParam(required = false) String putApplName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        BrowseFilter filter = BrowseFilter.builder()
                .limit(limit)
                .offset(offset)
                .msgId(msgId)
                .putUserId(putUserId)
                .putApplName(putApplName)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        List<BrowseMessageDTO> messages = mqAdminService.browseMessages(queueName, filter);
        int depth = mqAdminService.getQueueDepth(queueName);
        return Map.of(
                "code", 200,
                "message", "Browsed " + messages.size() + " messages from " + queueName,
                "depth", depth,
                "data", messages
        );
    }

    /**
     * 使用 JMS QueueBrowser 浏览队列消息（非破坏性）
     * GET /mq/browse/jms?queueName=DEV.QUEUE.1&limit=20&offset=0
     *
     * http://127.0.0.1:8080/mq/browse/jms?queueName=DEV.QUEUE.1&limit=10
     */
    @GetMapping("/browse/jms")
    public Map<String, Object> browseMessagesJms(
            @RequestParam String queueName,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String msgId,
            @RequestParam(required = false) String putUserId,
            @RequestParam(required = false) String putApplName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        BrowseFilter filter = BrowseFilter.builder()
                .limit(limit)
                .offset(offset)
                .msgId(msgId)
                .putUserId(putUserId)
                .putApplName(putApplName)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        List<BrowseMessageDTO> messages = jmsBrowseService.browseMessages(queueName, filter);
        int depth = mqAdminService.getQueueDepth(queueName);
        return Map.of(
                "code", 200,
                "message", "JMS browsed " + messages.size() + " messages from " + queueName,
                "depth", depth,
                "data", messages
        );
    }

    /**
     * 发送请求并同步等待回复（correlId 匹配模式）
     * GET /mq/request-reply?msg=hello&timeout=5000
     *
     * 请求发送到 send-queue（DEV.QUEUE.2），
     * 自动回复监听器（MqRequestAutoReplyListener）收到后回复到 receive-queue（DEV.QUEUE.1），
     * 通过 JMSCorrelationID 匹配请求与回复。
     *
     * http://127.0.0.1:8080/mq/request-reply?msg=hello
     */
    @GetMapping("/request-reply")
    public Map<String, Object> requestReply(
            @RequestParam(defaultValue = "Hello IBM MQ") String msg,
            @RequestParam(defaultValue = "10000") long timeout) {
        String reply = mqRequestReplyService.sendAndReceive(msg, timeout);
        if (reply != null) {
            return Map.of(
                    "code", 200,
                    "message", "Request-Reply completed",
                    "data", reply
            );
        }
        return Map.of(
                "code", 408,
                "message", "No reply received within timeout",
                "data", null
        );
    }
}
