package cn.reid.springjmsibm.controller;

import cn.reid.springjmsibm.service.MqAdminService;
import cn.reid.springjmsibm.service.MqMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mq")
@RequiredArgsConstructor
public class MqController {

    private final MqMessageService mqMessageService;
    private final MqAdminService mqAdminService;

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
     * http://127.0.0.1:8080/mq/send-to?queue=DEV.QUEUE.1&msg=hello
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
}
