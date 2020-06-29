package cn.hdj.argumentResolver;

import cn.hdj.argumentResolver.entity.Friend;
import cn.hdj.argumentResolver.entity.Order;
import cn.hdj.argumentResolver.entity.User;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
@SpringBootApplication
public class MvcArgumentResolverApplication {

    public static void main(String[] args) {
        SpringApplication.run(MvcArgumentResolverApplication.class, args);
    }


    @GetMapping("index")
    public String index() {
        return "index";
    }


    // ============ 基本类型


    /**
     * 请求形式
     * http://localhost:8080/baseType?a=1
     * <p>
     * 注意不带参数报错,以下两种形式
     * http://localhost:8080/baseType?a=
     * http://localhost:8080/baseType
     */
    @GetMapping("/baseType")
    public String baseType(int a) {
        return "baseType " + a;
    }

    /**
     * 请求形式
     * http://localhost:8080/baseType2
     * http://localhost:8080/baseType2?a=
     * http://localhost:8080/baseType2?a=1
     */
    @GetMapping("/baseType2")
    public String baseType2(Integer a) {
        return "baseType2 " + a;
    }


    /**
     * 请求形式
     * http://localhost:8080/baseType3?b=
     * http://localhost:8080/baseType3?b=123
     * <p>
     * 注意
     * http://localhost:8080/baseType3 报错
     *
     * @RequestParam 修改请求参数名称， 修改参数是否必传
     */
    @GetMapping("/baseType3")
    public String baseType3(@RequestParam(value = "b", required = true) Integer a) {
        return "baseType3 " + a;
    }


    //========== 对象类型


    /**
     * 请求形式
     * http://localhost:8080/complexType
     * http://localhost:8080/complexType?id=1&name=Java
     */
    @GetMapping("/complexType")
    public String complexType(User user) {
        return "complexType " + user;
    }

    /**
     * 请求形式(内嵌对象)
     * http://localhost:8080/complexType2
     * http://localhost:8080/complexType2?id=1&user.name=Java&user.id=2
     */
    @GetMapping("/complexType2")
    public String complexType2(Order order) {
        return "complexType2 " + order;
    }

    /**
     * 请求形式
     * http://localhost:8080/complexType3?name=Java  name会同时填充到User 和Friend对象上
     * http://localhost:8080/complexType3?user.name=Java
     * http://localhost:8080/complexType3?user.name=Java&friend.name=Python
     *
     * @see "https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-initbinder"
     */
    @GetMapping("/complexType3")
    public String complexType3(User user, Friend friend) {
        return "complexType3  user" + user + "  friend " + friend;
    }

    /**
     * 初始化绑定参数user 标识前缀
     *
     * @param binder
     */
    @InitBinder("user")
    public void initBinderUser(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("user.");
    }

    /**
     * 初始化绑定参数friend 标识前缀
     *
     * @param binder
     */
    @InitBinder("friend")
    public void initBinderFriend(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("friend.");
    }


    // 日期类型


    /**
     * 需要转换器
     *
     * 1. 配置转换器的方式
     * 2. 配置格式化器的方式
     */
    @GetMapping("/dateType")
    public String dateType(Date date) {
        return "dateType  date" + date;
    }

}
