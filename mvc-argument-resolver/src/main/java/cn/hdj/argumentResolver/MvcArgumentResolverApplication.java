package cn.hdj.argumentResolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
public class MvcArgumentResolverApplication {

    public static void main(String[] args) {
        SpringApplication.run(MvcArgumentResolverApplication.class, args);
    }


    @ResponseBody
    @GetMapping(value = {"/",""})
    public String index() {
        return "Hello World!";
    }


}
