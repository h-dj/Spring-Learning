package cn.hdj.modules.api.controller;

import cn.hdj.common.api.ResultVO;
import cn.hdj.modules.api.domain.MessageSendRequest;
import cn.hdj.modules.message.service.IMessageSendService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description: 消息中心 Rest API
 * @Author huangjiajian
 * @Date 2022/8/5 16:57
 */
@RestController
@RequestMapping(value = "/api/message")
public class MessageCenterController {

    @Resource
    private IMessageSendService messageSendService;

    @PostMapping(value = "/send")
    public ResultVO send(MessageSendRequest request){
        messageSendService.send(request);
        return ResultVO.successJson("ok");
    }
}
