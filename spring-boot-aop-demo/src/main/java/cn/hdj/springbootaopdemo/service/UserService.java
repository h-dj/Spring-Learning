package cn.hdj.springbootaopdemo.service;

import cn.hdj.springbootaopdemo.entity.UserDO;

import java.io.Serializable;
import java.util.List;

/**
 * @author hdj
 * @Description: 用户接口
 * @date 8/30/19
 */
public interface UserService {

    /**
     * 用户列表
     *
     * @return
     */
    List<UserDO> listUser();

    /**
     * 添加用户
     *
     * @param user
     */
    void saveUser(UserDO user);

    /**
     * 更新用户
     *
     * @param userId
     * @param user
     */
    void updateUser(Long userId, UserDO user);

    /**
     * 删除用户
     *
     * @param userId
     */
    void removeUser(Long userId);
}
