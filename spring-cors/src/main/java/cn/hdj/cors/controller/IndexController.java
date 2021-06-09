package cn.hdj.cors.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/4/18 上午12:31
 */

@Controller
public class IndexController {

    @ResponseBody
    @RequestMapping("index")
    public String indxe(HttpServletRequest request) {
        System.out.println("进入====>" + request.getRequestURI());
        return "Hello!";
    }
}
