package cn.hdj.message.message.sender;

import cn.hdj.message.entity.SysMessageTemplate;
import cn.hdj.message.message.domain.email.EmailMessageRequest;
import cn.hdj.message.message.domain.MessageRequest;
import cn.hdj.message.message.domain.MessageResponse;
import cn.hdj.message.message.domain.email.EmailMessageResponse;
import cn.hdj.message.service.SysMessageTemplateService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Set;

/**
 * @Description: 短信发送器
 * @Author cloud-inman
 * @Date 2022/2/22 11:41
 */
@Service
public class EmailMessageSender implements MessageSender {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private SysMessageTemplateService sysMessageTemplateService;


    public EmailMessageResponse send(EmailMessageRequest request) throws Exception {

        //获取模板
        SysMessageTemplate messageTemplate = this.sysMessageTemplateService.getById(request.getTemplateId());
        //填充模板
        //根据模板参数，填充模板
        String content = StrUtil.format(messageTemplate.getContent(), request.getVarMap());

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

        mimeMessage.setFrom(request.getFrom());
        //接收人
        List<String> toList = request.getToList();
        for (String email : toList) {
            messageHelper.addTo(email);
        }
        messageHelper.setSubject(messageTemplate.getTitle());
        messageHelper.setText(content,true);

        javaMailSender.send(mimeMessage);
        return new EmailMessageResponse();
    }


}
