package cn.hdj.modules.web.domain.from;

import cn.hdj.modules.web.entity.SysMessageType;
import lombok.Data;

import java.util.List;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/8/4 18:33
 */
@Data
public class SysMessageTypeForm extends SysMessageType {

    private List<SysMessageTypeRefForm> typeRefFormList;
}
