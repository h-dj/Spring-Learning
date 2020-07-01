package cn.hdj.argumentResolver;

import cn.hdj.argumentResolver.entity.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author hdj
 * @version 1.0
 * @date 30/06/2020 22:46
 * @description: SpringMVC 中不同类型的数据绑定
 */
@RestController
public class DemoDataBindingController extends BaseController {

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
     * http://localhost:8080/objectType
     * http://localhost:8080/objectType?id=1&name=Java
     */
    @GetMapping("/objectType")
    public String objectType(User user) {
        return "objectType " + user;
    }

    /**
     * 请求形式(内嵌对象)
     * http://localhost:8080/objectType2
     * http://localhost:8080/objectType2?id=1&user.name=Java&user.id=2
     */
    @GetMapping("/objectType2")
    public String objectType2(Order order) {
        return "objectType2 " + order;
    }

    /**
     * 请求形式
     * http://localhost:8080/objectType3?name=Java  name会同时填充到User 和Friend对象上
     * http://localhost:8080/objectType3?user.name=Java
     * http://localhost:8080/objectType3?user.name=Java&friend.name=Python
     *
     * @see "https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-initbinder"
     */
    @GetMapping("/objectType3")
    public String objectType3(User user, Friend friend) {
        return "objectType3  user" + user + "  friend " + friend;
    }


    // 日期类型


    /**
     * 日期转换
     * 1. @InitBinder("date") 参数绑定注册
     * 2. 实现 WebMvcConfigurer#addFormatters 方法注册  适用用全局配置
     * 3. 使用注解@DateTimeFormat 可灵活单独配置
     * <p>
     * 请求访问
     * http://localhost:8080/dateType?date=2020-01-01
     */
    @GetMapping("/dateType")
    public String dateType(Date date) {
        return "dateType  date" + date;
    }


    /**
     * http://localhost:8080/dateType?date1=2020-01-01
     */
    @GetMapping("/dateType2")
    public String dateType2(@DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE) Date date1) {
        return "dateType2  date" + date1;
    }


    //http://localhost:8080/dateType3?birthday=2020-01-01
    @GetMapping("/dateType3")
    public String dateType3(UserDate userDate) {
        return "dateType3  date" + userDate;
    }

    /**
     * 配置 @RequestBody注解， 是采用HttpMessageConverter进行转换
     * <p>
     * http://localhost:8080/dateType4
     * {
     * birthday："2020-01-01"
     * }
     */
    @PostMapping("/dateType4")
    @ResponseBody
    public UserDate dateType4(@RequestBody UserDate userDate) {
        return userDate;
    }


    // 复杂类型  List、Set、Map

    /**
     * 请求形式 (错误做法)
     * http://localhost:8080/complexType?list=1&list=2
     * <p>
     * 注意： 只能获取到第一个值 1
     */
    @GetMapping("/complexType")
    public String complexType(@ModelAttribute("list") List<String> list) {
        return "complexType " + list;
    }

    /**
     * 请求形式
     * http://localhost:8080/complexType2?list=1&list=2
     */
    @GetMapping("/complexType2")
    public String complexType2(@RequestParam("list") List<String> list) {
        return "complexType2 " + list;
    }

    /**
     * 请求形式
     * http://localhost:8080/complexType3?list=1&list=2
     */
    @GetMapping("/complexType3")
    public String complexType3(String[] list) {
        return "complexType3 " + Arrays.toString(list);
    }

    /**
     * 请求形式
     * http://localhost:8080/complexType4
     * <p>
     * 请求体
     * [1,2,3]
     */
    @PostMapping("/complexType4")
    public String complexType4(@RequestBody List<String> list) {
        return "complexType4 " + list;
    }


    /**
     * 请求形式(URL 需要编码  http://localhost:8080/complexType5?list[0]=1&list[1]=2)
     * http://localhost:8080/complexType5?list%5b0%5d=1&list%5b1%5d=2
     * <p>
     */
    @GetMapping("/complexType5")
    public String complexType5(UserList userList) {
        return "complexType5 " + userList;
    }


    //json 形式

    /**
     * 请求
     * http://localhost:8080/jsonType
     * 请求体
     * {
     * "id": 1,
     * "name": "Java"
     * }
     *
     * @RequestBody 不支持GET请求
     */
    @PostMapping(value = "/jsonType", consumes = "application/json")
    public String jsonType(@RequestBody User user) {
        return "jsonType " + user;
    }


    //xml形式

    /**
     * http://localhost:8080/xmlType
     *
     *
     * <?xml version="1.0" encoding="utf-8"?>
     * <user>
     * <id>1</id>
     * <name>Java</name>
     * </user>
     */
    @PostMapping(path = "/xmlType", consumes = "application/xml;charset=UTF-8")
    public String xmlType(@RequestBody User user) {
        return "xmlType " + user;
    }

}
