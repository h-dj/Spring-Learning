package cn.hdj.message.controller;


import cn.hdj.message.entity.SysMessageTemplate;
import cn.hdj.message.service.SysMessageTemplateService;
import cn.hdj.message.service.impl.SysMessageTemplateServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Wrapper;
import java.util.List;

/**
 * <p>
 * 系统消息模板 前端控制器
 * </p>
 *
 * @author huangjiajian
 * @since 2022-02-23
 */
@RestController
@RequestMapping("/messageTemplate")
public class SysMessageTemplateController {

    @Autowired
    private  SysMessageTemplateService sysMessageTemplateService;


    /**
     * 模板列表
     *
     * @return
     */
    @GetMapping(value = "/list")
    public ResponseEntity<List<SysMessageTemplate>> queryList() {
        LambdaQueryWrapper<SysMessageTemplate> queryWrapper = Wrappers.<SysMessageTemplate>lambdaQuery()
                .eq(SysMessageTemplate::getIsDelete, "0");
        return ResponseEntity.ok(this.sysMessageTemplateService.list(queryWrapper));
    }


    /**
     * 添加模板
     */
    /**
     * 模板列表
     *
     * @return
     */
    @PostMapping(value = "/add")
    public ResponseEntity add(SysMessageTemplate sysMessageTemplate) {
        this.sysMessageTemplateService.save(sysMessageTemplate);
        return ResponseEntity
                .ok("ok");
    }


    /**
     * 删除模板
     */

    @PostMapping(value = "/delete")
    public ResponseEntity delete(Integer sysMessageTemplateId) {

        SysMessageTemplate sysMessageTemplate=new SysMessageTemplate();
        sysMessageTemplate.setId(sysMessageTemplateId);
        sysMessageTemplate.setIsDelete("1");
        this.sysMessageTemplateService.updateById(sysMessageTemplate);
        return ResponseEntity
                .ok("ok");
    }
}

