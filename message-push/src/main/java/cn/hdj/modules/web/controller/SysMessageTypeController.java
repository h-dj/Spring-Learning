package cn.hdj.modules.web.controller;


import cn.hdj.common.api.ResultVO;
import cn.hdj.modules.web.domain.from.SysMessageTypeForm;
import cn.hdj.modules.web.domain.vo.SysMessageTypeVO;
import cn.hdj.modules.web.entity.SysMessageType;
import cn.hdj.modules.web.entity.SysMessageTypeRef;
import cn.hdj.modules.web.service.ISysMessageTypeRefService;
import cn.hdj.modules.web.service.ISysMessageTypeService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 消息模板类型 前端控制器
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
@RestController
@RequestMapping("/web/sysMessageType")
public class SysMessageTypeController {


    @Resource
    private ISysMessageTypeService sysMessageTypeService;

    @Resource
    private ISysMessageTypeRefService sysMessageTypeRefService;


    @GetMapping(value = "/list")
    public ResultVO list() {
        List<SysMessageType> messageTypeList = this.sysMessageTypeService.list();
        return ResultVO.successJson(messageTypeList);
    }


    @GetMapping(value = "/{messageTypeId}")
    public ResultVO getDetailById(@PathVariable("messageTypeId") Long messageTypeId) {

        SysMessageType messageType = this.sysMessageTypeService.getById(messageTypeId);

        LambdaQueryWrapper<SysMessageTypeRef> queryWrapper = Wrappers.<SysMessageTypeRef>lambdaQuery()
                .eq(SysMessageTypeRef::getTempServerId, messageTypeId);
        List<SysMessageTypeRef> messageTypeRefList = this.sysMessageTypeRefService.list(queryWrapper);


        SysMessageTypeVO sysMessageTypeVO = BeanUtil.copyProperties(messageType, SysMessageTypeVO.class);
        sysMessageTypeVO.setMessageTypeRefList(messageTypeRefList);
        return ResultVO.successJson(sysMessageTypeVO);
    }


    @PostMapping(value = "/add")
    public ResultVO addMessageType(@RequestBody @Validated SysMessageTypeForm messageTypeForm) {
        this.sysMessageTypeService.saveUpdateMessageType(messageTypeForm);
        return ResultVO.successJson("ok");
    }


    @PostMapping(value = "/update/{messageTypeId}")
    public ResultVO addMessageType(@PathVariable("messageTypeId") Long messageTypeId,@RequestBody @Validated SysMessageTypeForm messageTypeForm) {
        messageTypeForm.setId(messageTypeId);
        this.sysMessageTypeService.saveUpdateMessageType(messageTypeForm);
        return ResultVO.successJson("ok");
    }

}

