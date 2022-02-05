package cn.hdj.scene.service;

import cn.hdj.scene.entity.UserDTO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 用户服务
 * @Author huangjiajian
 * @Date 2022/1/30 下午3:46
 */
@Service
public class UserService {


    @Autowired
    private RedissonClient redisClient;


    /**
     * redis 缓存使用
     * @param userId
     * @return
     */
    @Cacheable(cacheNames = "user",key = "#userId")
    public UserDTO getUser(Long userId){
        System.out.println("============ 从数据库获取用户 =======");

        UserDTO userDTO=new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setRoles(new ArrayList<>(Arrays.asList(new String[]{"normal","admin"})));

        return userDTO;
    }


    /**
     * redis 分布式锁使用
     * @param userId
     * @throws InterruptedException
     */
    public void updateById(Long userId) throws InterruptedException {
        RLock lock = redisClient.getLock("lock:user:" + userId);
        if(lock.tryLock()){
            try {
                System.out.println("============更新用户 =======");
                TimeUnit.SECONDS.sleep(5);
            }finally {
                lock.unlock();
            }
        }
        System.out.println("============更新用户: 获取锁失败！ =======");
    }
}
