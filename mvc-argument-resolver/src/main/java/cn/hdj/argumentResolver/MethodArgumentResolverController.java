package cn.hdj.argumentResolver;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author hdj
 * @version 1.0
 * @date 7/1/20 4:18 PM
 * @description:
 */
@RestController
public class MethodArgumentResolverController {

    // 以下都采用RequestParamMethodArgumentResolver 解析

    @GetMapping("/requestParam")
    public String requestParam(int a) {
        return "requestParam " + a;
    }

    @GetMapping("/requestParam2")
    public String requestParam2(@RequestParam("queryA") int a) {
        return "requestParam2 " + a;
    }


    //RequestParamMapMethodArgumentResolver


    /**
     * http://localhost:8080/requestParamMap?a=1
     * <p>
     * 注意 @RequestParam 不能设置参数名称； 如@RequestParam("map")
     */
    @GetMapping("/requestParamMap")
    public String requestParamMap(@RequestParam Map<String, Object> map) {
        return "requestParamMap " + map;
    }


    /**
     * http://localhost:8080//pathVariable/11111
     */
    @GetMapping("/pathVariable/{a}")
    public String pathVariable(@PathVariable("a") int a) {
        return "pathVariable " + a;
    }


    /**
     * http://localhost:8080//pathVariable/Java/25
     */
    @GetMapping("/pathVariable/{name}/{value}")

    public String pathVariable(@PathVariable Map<String, Object> map) {
        return "pathVariable " + map;
    }

    /**
     *  http://localhost:8080//employeesContacts/contactNumber=123456789
     */
    @GetMapping("/employeesContacts/{contactNumber}")
    public String getEmployeeBycontactNumber(@MatrixVariable(required = true) String contactNumber) {
        return "employeesContacts" + contactNumber;
    }

    /**
     * http://localhost:8080/employeeData/id=1;name=John;contactNumber=2200112334
     */
    @GetMapping("employeeData/{employee}")
    public String getEmployeeData(@MatrixVariable Map<String, String> matrixVars) {
        return "employeesContacts" + matrixVars;
    }
}