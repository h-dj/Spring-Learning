package cn.hdj.fastboot.modules.test.enums;

import cn.hdj.fastboot.modules.test.strategy.*;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/10/21 18:08
 */
public enum CrmNumberSettingTypeEnum {

    //编号类型 1文本 2日期 3数字 4json映射

    TEXT(1, "文本", TextNumberSettingTypeStrategy.class),
    DATE(2, "日期", DateNumberSettingTypeStrategy.class),
    NUMBER(3, "数字", AutoIncrementNumberSettingTypeStrategy.class),

    /**
     * {
     * "field":{
     * "code1":"value1",
     * "code2":"value2",
     * "code3":"value3",
     * }
     * }
     */
    JSON(4, "json映射", JsonMappingNumberSettingTypeStrategy.class),
    ;
    Integer code;
    String value;
    Class strategy;

    CrmNumberSettingTypeEnum(Integer code, String value, Class strategy) {
        this.code = code;
        this.value = value;
        this.strategy = strategy;
    }

    public static Class<INumberSettingTypeStrategy> getStrategy(Integer type) {
        for (CrmNumberSettingTypeEnum typeEnum : values()) {
            if (typeEnum.code.equals(type)) {
                return typeEnum.strategy;
            }
        }
        throw new IllegalArgumentException("编号类型不存在！ type=" + type);
    }
}
