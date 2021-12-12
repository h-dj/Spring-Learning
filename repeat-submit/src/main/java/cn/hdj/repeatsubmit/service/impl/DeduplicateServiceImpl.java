package cn.hdj.repeatsubmit.service.impl;

import cn.hdj.repeatsubmit.po.DeduplicatePO;
import cn.hdj.repeatsubmit.mapper.DeduplicateMapper;
import cn.hdj.repeatsubmit.service.IDeduplicateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 去重表 服务实现类
 * </p>
 *
 * @author huangjiajian
 * @since 2021-12-12
 */
@Service
public class DeduplicateServiceImpl extends ServiceImpl<DeduplicateMapper, DeduplicatePO> implements IDeduplicateService {

}
