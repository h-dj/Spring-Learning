package cn.hdj.modules.web.controller;


import cn.hdj.common.api.ResultVO;
import cn.hdj.modules.web.domain.from.SysMessageTemplateForm;
import cn.hdj.modules.web.entity.SysMessageTemplate;
import cn.hdj.modules.web.service.ISysMessageTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 消息模板表 前端控制器
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
@RestController
@RequestMapping("/web/sysMessageTemplate")
public class SysMessageTemplateController {

    @Resource
    private ISysMessageTemplateService sysMessageTemplateService;

    @GetMapping(value = "/list")
    public ResultVO list() {
        LambdaQueryWrapper<SysMessageTemplate> queryWrapper = Wrappers.<SysMessageTemplate>lambdaQuery()
                .eq(SysMessageTemplate::getEnabledFlag, "1");
        List<SysMessageTemplate> list = this.sysMessageTemplateService.list(queryWrapper);
        return ResultVO.successJson(list);
    }


    @GetMapping(value = "/{templateId}")
    public ResultVO getDetailById(@PathVariable("templateId") Long templateId) {
        SysMessageTemplate messageTemplate = this.sysMessageTemplateService.getById(templateId);
        return ResultVO.successJson(messageTemplate);
    }


    @PostMapping(value = "/add")
    public ResultVO addTemplate(@RequestBody @Validated SysMessageTemplateForm messageTemplateFrom) {
        this.sysMessageTemplateService.save(messageTemplateFrom);
        return ResultVO.successJson("ok");
    }


    @PostMapping(value = "/update/{templateId}")
    public ResultVO updateTemplate(@PathVariable("templateId") Long templateId, @RequestBody @Validated SysMessageTemplateForm messageTemplateFrom) {
        messageTemplateFrom.setId(templateId);
        this.sysMessageTemplateService.updateById(messageTemplateFrom);
        return ResultVO.successJson("ok");
    }


    @PostMapping(value = "/delete/{templateId}")
    public ResultVO deleteTemplate(@PathVariable("templateId") Long templateId) {
        LambdaUpdateWrapper<SysMessageTemplate> updateWrapper = Wrappers.<SysMessageTemplate>lambdaUpdate()
                .eq(SysMessageTemplate::getId, templateId)
                .set(SysMessageTemplate::getEnabledFlag, "0");
        this.sysMessageTemplateService.update(updateWrapper);
        return ResultVO.successJson("ok");
    }

}

