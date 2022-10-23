package cn.hdj.fastboot.modules.test.controller;


import cn.hdj.fastboot.common.api.ResultVO;
import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import cn.hdj.fastboot.modules.test.service.ICrmNumberSettingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.web.bind.annotation.*;

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
@Api(tags = "编号设置")
@RestController
@RequestMapping("/test/crmNumberSetting")
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CrmNumberSettingController {


    private final ICrmNumberSettingService crmNumberSettingService;

    @PostMapping(value = "/addNumConfig")
    public ResultVO addNumConfig(@RequestBody CrmNumberSetting config) {
        this.crmNumberSettingService.addNumSetting(config);
        return ResultVO.successJson("ok");
    }

    @GetMapping(value = "/list")
    public ResultVO list(@RequestParam("code") String code) {
        LambdaQueryWrapper<CrmNumberSetting> queryWrapper = Wrappers.<CrmNumberSetting>lambdaQuery()
                .eq(CrmNumberSetting::getCode, code)
                .orderByDesc(CrmNumberSetting::getSort);
        List<CrmNumberSetting> list = this.crmNumberSettingService.list(queryWrapper);
        return ResultVO.successJson(list);
    }

    @GetMapping(value = "/doGenerate")
    public ResultVO doGenerate(@RequestParam("code") String code, Map<String,Object> map){
        String num = this.crmNumberSettingService.generate(map,code);
        return ResultVO.successJson(num);
    }


}

