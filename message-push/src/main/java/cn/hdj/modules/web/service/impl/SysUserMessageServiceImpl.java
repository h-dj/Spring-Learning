package cn.hdj.modules.web.service.impl;

import cn.hdj.modules.web.entity.SysUserMessage;
import cn.hdj.modules.web.mapper.SysUserMessageMapper;
import cn.hdj.modules.web.service.ISysUserMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户消息表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2022-08-04
 */
@Service
public class SysUserMessageServiceImpl extends ServiceImpl<SysUserMessageMapper, SysUserMessage> implements ISysUserMessageService {

}
