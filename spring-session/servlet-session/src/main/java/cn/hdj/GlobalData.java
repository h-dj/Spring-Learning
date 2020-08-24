package cn.hdj;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/24 下午4:57
 * @description: 全局数据缓存
 */
public class GlobalData {

    private static ConcurrentHashMap map = new ConcurrentHashMap();

    public static void set(String key, Object value) {
        map.put(key, value);
    }

    public static Object get(String key) {
        return map.get(key);
    }

    public static void remove(String key) {
        map.remove(key);
    }
}
