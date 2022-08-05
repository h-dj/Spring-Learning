package cn.hdj.modules.web.controller;


import cn.hdj.common.api.ResultVO;
import cn.hdj.modules.web.entity.SysMessageType;
import cn.hdj.modules.web.entity.SysUserMessageConfig;
import cn.hdj.modules.web.service.ISysMessageTypeService;
import cn.hdj.modules.web.service.ISysUserMessageConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户消息配置 前端控制器
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
@RestController
@RequestMapping("/web/sysUserMessageConfig")
public class SysUserMessageConfigController {

    public Long getCurrentUserId() {
        return 1L;
    }

    @Resource
    private ISysMessageTypeService sysMessageTypeService;
    @Resource
    private ISysUserMessageConfigService sysUserMessageConfigService;

    @GetMapping(value = "/list")
    public ResultVO list() {
        List<SysUserMessageConfig> list = this.sysUserMessageConfigService.list();
        return ResultVO.successJson(list);
    }


    @PostMapping(value = "/init")
    public ResultVO init() {

        //查询消息类型
        List<SysMessageType> list = this.sysMessageTypeService.list();

        List<SysUserMessageConfig> messageConfigList = list.stream()
                .map(v -> {
                    SysUserMessageConfig config = new SysUserMessageConfig();
                    config.setUserId(getCurrentUserId());
                    config.setMessageCode(v.getMessageCode());
                    config.setWeb("1");
                    return config;
                })
                .collect(Collectors.toList());

        LambdaQueryWrapper<SysUserMessageConfig> queryWrapper = Wrappers.<SysUserMessageConfig>lambdaQuery()
                .eq(SysUserMessageConfig::getUserId, getCurrentUserId());

        this.sysUserMessageConfigService.remove(queryWrapper);

        this.sysUserMessageConfigService.saveBatch(messageConfigList);

        return ResultVO.successJson("ok");
    }



}

