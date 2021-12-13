package cn.hdj.repeatsubmit.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/12/13 下午11:05
 */
public class ZookeeperLockUtil {

    /**
     * 节点名称
     */
    public static final String NODE_PATH = "/lock-space/%s";


    static volatile CuratorFramework curatorFramework;

    public static void setCuratorFramework(CuratorFramework c) {
        curatorFramework = c;
    }

    /**
     * 尝试获取分布式锁
     *
     * @param key        分布式锁 key
     * @param expireTime 超时时间
     * @param timeUnit   时间单位
     * @return 超时时间单位
     */
    public static InterProcessMutex tryLock(String key, long expireTime, TimeUnit timeUnit) {
        try {
            InterProcessMutex mutex = new InterProcessMutex(curatorFramework, String.format(NODE_PATH, key));
            boolean locked = mutex.acquire(expireTime, timeUnit);
            if (locked) {
                System.out.println("申请锁(" + key + ")成功！");
                return mutex;
            }
        } catch (Exception e) {
            System.out.println("申请锁(" + key + ")成功！");
        }
        System.err.println("申请锁(" + key + ")失败！");
        return null;
    }

    /**
     * 释放锁
     *
     * @param key          分布式锁 key
     * @param lockInstance InterProcessMutex 实例
     */
    public static void unLock(String key, InterProcessMutex lockInstance) {
        try {
            lockInstance.release();
            System.out.println("解锁(" + key + ")成功！");
        } catch (Exception e) {
            System.err.println("解锁(" + key + ")失败！");
        }
    }


}
