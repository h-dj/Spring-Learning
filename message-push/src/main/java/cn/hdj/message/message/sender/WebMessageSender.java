package cn.hdj.message.message.sender;

import cn.hdj.message.entity.SysMessageContent;
import cn.hdj.message.entity.SysMessageTemplate;
import cn.hdj.message.entity.SysUserMessageNotice;
import cn.hdj.message.message.domain.web.WebMsgRequest;
import cn.hdj.message.message.domain.web.WebMsgResponse;
import cn.hdj.message.service.SysMessageContentService;
import cn.hdj.message.service.SysMessageTemplateService;
import cn.hdj.message.service.SysUserMessageNoticeService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 短信发送器
 * @Author cloud-inman
 * @Date 2022/2/22 11:41
 */
@Service
public class WebMessageSender implements MessageSender {

    @Autowired
    private SysMessageContentService sysMessageContentService;
    @Autowired
    private SysUserMessageNoticeService sysUserMessageNoticeService;
    @Autowired
    private SysMessageTemplateService sysMessageTemplateService;


    /**
     * 发送
     *
     * @param request
     */
    @Transactional(rollbackFor = Exception.class)
    public WebMsgResponse send(WebMsgRequest request) {
        //通过模板ID 获取模板
        SysMessageTemplate sysMessageTemplate = this.sysMessageTemplateService.getById(request.getTemplateId());
        //根据模板参数，填充模板
        String content = StrUtil.format(sysMessageTemplate.getContent(), request.getVarMap());

        SysMessageContent messageContent = SysMessageContent.builder()
                .context(content)
                .sendUserId(request.getSendUserId())
                .publishTime(new Date())
                .title(sysMessageTemplate.getTitle())
                .build();

        this.sysMessageContentService.save(messageContent);
        List<Integer> receiveUserIds = request.getReceiveUserIds();
        List<SysUserMessageNotice> userMessageNoticeList = receiveUserIds.stream()
                .map(receiveUserId -> {
                    return SysUserMessageNotice.builder()
                            .createTime(new Date())
                            .messageContentId(messageContent.getId())
                            .isRead("0")
                            .receiverUserId(receiveUserId)
                            .updateTime(new Date())
                            .build();
                })
                .collect(Collectors.toList());
        this.sysUserMessageNoticeService.saveBatch(userMessageNoticeList);

        return new WebMsgResponse();
    }
}
