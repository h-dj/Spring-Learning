package cn.hdj.springbootaopdemo.proxy.jdk;

public class Taxi implements Car{
    @Override
    public void running() {
        System.out.println("The taxi is running.");
    }
}
