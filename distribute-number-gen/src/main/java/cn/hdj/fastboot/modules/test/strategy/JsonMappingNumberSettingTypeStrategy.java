package cn.hdj.fastboot.modules.test.strategy;

import cn.hdj.fastboot.modules.test.entity.CrmNumberSetting;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author huangjiajian
 * @Date 2022/10/21 下午10:09
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Component
public class JsonMappingNumberSettingTypeStrategy implements INumberSettingTypeStrategy {


    @Override
    public String parse(CrmNumberSetting setting, Map<String, Object> params) {
        String json = setting.getValue();

        LinkedHashMap<String, LinkedHashMap<String, String>> map = JSONUtil.parse(json).toBean(new TypeReference<LinkedHashMap<String, LinkedHashMap<String, String>>>() {
        });
        StringBuilder builder = new StringBuilder(16);
        for (Map.Entry<String, LinkedHashMap<String, String>> entry : map.entrySet()) {
            String key = entry.getKey();
            Object o = params.get(key);
            LinkedHashMap<String, String> hashMap = entry.getValue();
            String value = hashMap.get(StrUtil.str(o, CharsetUtil.CHARSET_UTF_8));
            builder.append(value);
        }
        return builder.toString();
    }
}
