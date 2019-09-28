package cn.hdj.mvninterceptor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hdj
 * @Description:
 * @date 9/28/19
 */
@Controller
public class HelloController {


    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        String msg = "say hello world!";
        System.out.println(msg);
        return msg;
    }
}
