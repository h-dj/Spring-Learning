package cn.hdj.fastboot.modules.test.controller;


import cn.hdj.fastboot.common.api.ResultVO;
import cn.hdj.fastboot.modules.test.entity.CrmNumberConfig;
import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import cn.hdj.fastboot.modules.test.service.ICrmNumberConfigService;
import cn.hdj.fastboot.modules.test.service.ICrmNumberSettingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统自动生成编号设置表 前端控制器
 * </p>
 *
 * @author huangjiajian
 * @since 2022-10-21
 */
@Controller
@RequestMapping("/test/crmNumberSetting")
@AllArgsConstructor
public class CrmNumberSettingController {


    private final ICrmNumberSettingService crmNumberSettingService;


    @PostMapping(name = "/addNumConfig")
    public ResultVO addNumConfig(@RequestBody CrmNumberSetting config) {
        this.crmNumberSettingService.addNumSetting(config);
        return ResultVO.successJson("ok");
    }

    @GetMapping(name = "/list")
    public ResultVO list(@RequestParam("code") String code) {
        LambdaQueryWrapper<CrmNumberSetting> queryWrapper = Wrappers.<CrmNumberSetting>lambdaQuery()
                .eq(CrmNumberSetting::getCode, code);
        List<CrmNumberSetting> list = this.crmNumberSettingService.list(queryWrapper);
        return ResultVO.successJson(list);
    }


    @GetMapping(name = "/generate")
    public ResultVO generate(Map<String,Object> map,String code){
        String num = this.crmNumberSettingService.generate(map,code);
        return ResultVO.successJson(num);
    }


}

