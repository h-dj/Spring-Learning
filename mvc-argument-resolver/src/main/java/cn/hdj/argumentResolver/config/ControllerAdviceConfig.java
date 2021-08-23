package cn.hdj.argumentResolver.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/8/19 17:36
 */
@ControllerAdvice
public class ControllerAdviceConfig {

    @InitBinder
    public void initBinderUser(WebDataBinder binder) {
        System.out.println("ControllerAdvice  WebDataBinder 执行" );
    }
}
