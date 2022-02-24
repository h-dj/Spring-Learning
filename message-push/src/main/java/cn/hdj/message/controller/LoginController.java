package cn.hdj.message.controller;

import cn.hdj.message.domain.LoginFormDTO;
import cn.hdj.message.entity.SysMessageTemplate;
import cn.hdj.message.entity.SysUserMessageSetting;
import cn.hdj.message.message.domain.email.EmailMessageRequest;
import cn.hdj.message.message.domain.email.EmailMessageResponse;
import cn.hdj.message.message.domain.web.WebMsgRequest;
import cn.hdj.message.message.domain.web.WebMsgResponse;
import cn.hdj.message.message.sender.EmailMessageSender;
import cn.hdj.message.message.sender.WebMessageSender;
import cn.hdj.message.service.SysMessageContentService;
import cn.hdj.message.service.SysMessageTemplateService;
import cn.hdj.message.service.SysUserMessageSettingService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2022/2/23 16:28
 */
@RestController
@RequestMapping(value = "/")
public class LoginController {


    @Autowired
    private SysUserMessageSettingService sysUserMessageSettingService;
    @Autowired
    private WebMessageSender webMessageSender;
    @Autowired
    private EmailMessageSender emailMessageSender;

    /**
     * 登录
     *
     * @return
     */
    @PostMapping(value = "/login")
    public ResponseEntity login(LoginFormDTO loginFormDTO) throws Exception {
        //登录

        //发送登录成功，送积分通知
        // 发送文案： 尊敬的用户{}，欢迎你回来，本次登录将获取 10 积分
        //1、查询用户消息设置
        LambdaQueryWrapper<SysUserMessageSetting> queryWrapper = Wrappers.<SysUserMessageSetting>lambdaQuery()
                .eq(SysUserMessageSetting::getMessageTypeCode, "verification_code");
        List<SysUserMessageSetting> messageSettingList = this.sysUserMessageSettingService.list(queryWrapper);

        if (CollectionUtil.isEmpty(messageSettingList)) {
            //如果为空，则设置默认设置
            SysUserMessageSetting sysUserMessageSetting = new SysUserMessageSetting();
            sysUserMessageSetting.setUserId(getUserId());
            sysUserMessageSetting.setEmail("1");
            sysUserMessageSetting.setSms("1");
            sysUserMessageSetting.setWeb("1");
            sysUserMessageSetting.setWechat("1");
            sysUserMessageSetting.setMessageTypeCode("verification_code");
            sysUserMessageSettingService.save(sysUserMessageSetting);

            messageSettingList.add(sysUserMessageSetting);
        }

        //接收人
        Integer userId = getUserId();
        //1、获取组装参数
        String userName = getUserName(userId);

        Map<String, Object> map = MapUtil.<String, Object>builder()
                .put("userName", userName)
                .build();

        SysUserMessageSetting userMessageSetting = messageSettingList.get(0);
        if(StrUtil.equals(userMessageSetting.getEmail(),"1")){
            EmailMessageRequest request = EmailMessageRequest.builder()
                    .templateId("1")
                    .from("1432517356@qq.com")
                    .varMap(map)
                    .toList(CollectionUtil.toList("1272196984@qq.com"))
                    .build();


            EmailMessageResponse messageResponse = this.emailMessageSender.send(request);

        }

        if(StrUtil.equals(userMessageSetting.getWeb(),"1")){
            WebMsgRequest request = WebMsgRequest.builder()
                    .receiveUserIds(ListUtil.of(userId))
                    .sendUserId(0)
                    .templateId("1")
                    .varMap(map)
                    .build();
            WebMsgResponse webMsgResponse = this.webMessageSender.send(request);
        }


        return ResponseEntity.ok(
                MapUtil.builder()
                        .put("success", true)
                        .put("msg", "ok")
                        .put("code", 200)
                        .build()
        );
    }


    public Integer getUserId() {
        return 11;
    }

    public String getUserName(Integer userId) {
        return "用户" + userId;
    }


}
