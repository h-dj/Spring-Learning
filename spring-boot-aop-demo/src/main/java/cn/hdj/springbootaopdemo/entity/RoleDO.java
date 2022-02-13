package cn.hdj.springbootaopdemo.entity;


/**
 * @author hdj
 * @Description: 角色
 * @date 8/30/19
 */
public class RoleDO {

    private Long id;

    private String roleName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
