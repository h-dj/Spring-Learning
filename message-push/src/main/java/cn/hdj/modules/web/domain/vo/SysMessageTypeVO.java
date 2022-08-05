package cn.hdj.modules.web.domain.vo;

import cn.hdj.modules.web.entity.SysMessageType;
import cn.hdj.modules.web.entity.SysMessageTypeRef;
import lombok.Data;

import java.util.List;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/8/5 9:59
 */
@Data
public class SysMessageTypeVO extends SysMessageType {


    List<SysMessageTypeRef> messageTypeRefList;
}
