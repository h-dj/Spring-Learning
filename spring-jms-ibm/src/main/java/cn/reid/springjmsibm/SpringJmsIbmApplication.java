package cn.reid.springjmsibm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@SpringBootApplication
public class SpringJmsIbmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringJmsIbmApplication.class, args);
    }

}
