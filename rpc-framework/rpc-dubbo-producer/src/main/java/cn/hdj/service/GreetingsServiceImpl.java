package cn.hdj.service;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/8 上午11:59
 * @description:
 */
public class GreetingsServiceImpl implements GreetingsService {
    @Override
    public String sayHi(String name) {
        return "hi, " + name;
    }
}
