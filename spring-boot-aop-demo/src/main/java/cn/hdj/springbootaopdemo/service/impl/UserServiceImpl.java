package cn.hdj.springbootaopdemo.service.impl;

import cn.hdj.springbootaopdemo.entity.UserDO;
import cn.hdj.springbootaopdemo.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author hdj
 * @Description:
 * @date 8/30/19
 */
@Service("user")
public class UserServiceImpl implements UserService {


    @Override
    public List<UserDO> listUser() {
        try {
            //模拟操作耗时
            Thread.sleep(new Random().nextInt(1000));
            System.out.println("UserServiceImpl-----listUser");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UserDO userDO = new UserDO();
        userDO.setId(123L);
        return Arrays.asList(userDO);
    }



    @Override
    public void saveUser(UserDO user) {
        try {
            //模拟操作耗时
            Thread.sleep(new Random().nextInt(1000));
            System.out.println("UserServiceImpl-----saveUser");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUser(Long userId, UserDO user) {
        try {
            //模拟操作耗时
            Thread.sleep(new Random().nextInt(1000));
            System.out.println("UserServiceImpl-----updateUser");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUser(Long userId) {
        System.out.println("UserServiceImpl-----removeUser");
        throw new RuntimeException();
    }

}
