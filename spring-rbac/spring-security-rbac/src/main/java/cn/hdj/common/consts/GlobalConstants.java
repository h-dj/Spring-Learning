package cn.hdj.common.consts;

/**
 * @Author huangjiajian
 * @Date 2022/6/26 下午1:13
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
public interface GlobalConstants {

    /**
     * 全局状态-是
     */
    Integer STATUS_YES = 1;

    /**
     * 超级管理员角色编码
     */
    String ROOT_ROLE_CODE = "ROOT";

    /**
     * [ {接口路径:[角色编码]},...]
     */
    String URL_PERM_ROLES_KEY = "system:perm_roles_rule:url";

    /**
     * [{按钮权限:[角色编码]},...]
     */
    String BTN_PERM_ROLES_KEY = "system:perm_roles_rule:btn";

    /**
     * JWT存储权限前缀
     */
    String AUTHORITY_PREFIX = "ROLE_";


    /**
     * 短信验证码key前缀
     */
    String SMS_CODE_PREFIX = "SMS_CODE:";

}
