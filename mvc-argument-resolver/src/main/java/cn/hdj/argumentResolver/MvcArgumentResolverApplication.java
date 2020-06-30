package cn.hdj.argumentResolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;


@SpringBootApplication
public class MvcArgumentResolverApplication {

    public static void main(String[] args) {
        SpringApplication.run(MvcArgumentResolverApplication.class, args);
    }


    @GetMapping("index")
    public String index() {
        return "index";
    }


}
