package cn.hdj.modules.web.service.impl;

import cn.hdj.modules.web.entity.SysMessage;
import cn.hdj.modules.web.mapper.SysMessageMapper;
import cn.hdj.modules.web.service.ISysMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息主表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {

}
