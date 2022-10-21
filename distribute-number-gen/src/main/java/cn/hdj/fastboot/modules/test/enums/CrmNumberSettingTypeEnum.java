package cn.hdj.fastboot.modules.test.enums;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/10/21 18:08
 */
public enum CrmNumberSettingTypeEnum {

    //编号类型 1文本 2日期 3数字 4json映射

    TEXT(1,"文本"),
    DATE(2,"日期"),
    NUMBER(3,"数字"),

    /**
     * {
     *     "field":{
     *         "code1":"value1",
     *         "code2":"value2",
     *         "code3":"value3",
     *     }
     * }
     */
    JSON(4,"json映射"),
    ;
    Integer code;
    String value;
    Class strategy;

    CrmNumberSettingTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }
}
