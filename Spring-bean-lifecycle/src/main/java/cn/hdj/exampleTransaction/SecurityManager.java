package cn.hdj.exampleTransaction;

/**
 * @author hdj
 * @version 1.0
 * @date 2019/10/3 12:13
 * @description:
 */
public class SecurityManager {

    private UserRealm userRealm;

    public SecurityManager(UserRealm userRealm) {
        this.userRealm = userRealm;
    }
}
