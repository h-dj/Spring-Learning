package cn.hdj.exampleTransaction;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author hdj
 * @version 1.0
 * @date 2019/10/3 10:15
 * @description:
 */

public class ShiroFilterFactoryBean implements FactoryBean {

    private SecurityManager securityManager;


    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }
    @Override
    public Object getObject() throws Exception {
        return securityManager ;
    }
    @Override
    public Class<?> getObjectType() {
        return SecurityManager.class;
    }
}
