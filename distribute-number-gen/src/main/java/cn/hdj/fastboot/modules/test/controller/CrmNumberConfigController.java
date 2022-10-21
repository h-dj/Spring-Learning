package cn.hdj.fastboot.modules.test.controller;


import cn.hdj.fastboot.common.api.ResultVO;
import cn.hdj.fastboot.modules.test.entity.CrmNumberConfig;
import cn.hdj.fastboot.modules.test.service.ICrmNumberConfigService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 * 系统自动生成编号配置表 前端控制器
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
@Controller
@RequestMapping("/test/crmNumberConfig")
@AllArgsConstructor
public class CrmNumberConfigController {

    private final ICrmNumberConfigService crmNumberConfigService;


    @PostMapping(name = "/addNumConfig")
    public ResultVO addNumConfig(@RequestBody CrmNumberConfig config) {
        this.crmNumberConfigService.addNumConfig(config);
        return ResultVO.successJson("ok");
    }

    @GetMapping(name = "/list")
    public ResultVO list() {
        List<CrmNumberConfig> list = this.crmNumberConfigService.list();
        return ResultVO.successJson(list);
    }
}

