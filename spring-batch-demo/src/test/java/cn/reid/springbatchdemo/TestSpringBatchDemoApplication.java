package cn.reid.springbatchdemo;

import org.springframework.boot.SpringApplication;

public class TestSpringBatchDemoApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringBatchDemoApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
