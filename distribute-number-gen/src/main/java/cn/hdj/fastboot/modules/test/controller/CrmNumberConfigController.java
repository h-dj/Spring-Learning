package cn.hdj.fastboot.modules.test.controller;


import cn.hdj.fastboot.common.api.ResultVO;
import cn.hdj.fastboot.modules.test.entity.CrmNumberConfig;
import cn.hdj.fastboot.modules.test.service.ICrmNumberConfigService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 系统自动生成编号配置表 前端控制器
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
@RestController
@RequestMapping("/test/crmNumberConfig")
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CrmNumberConfigController {

    private final ICrmNumberConfigService crmNumberConfigService;


    @PostMapping(value = "/addNumConfig")
    public ResultVO addNumConfig(@RequestBody CrmNumberConfig config) {
        this.crmNumberConfigService.addNumConfig(config);
        return ResultVO.successJson("ok");
    }

    @GetMapping(value = "/list")
    public ResultVO list() {
        List<CrmNumberConfig> list = this.crmNumberConfigService.list();
        return ResultVO.successJson(list);
    }
}

