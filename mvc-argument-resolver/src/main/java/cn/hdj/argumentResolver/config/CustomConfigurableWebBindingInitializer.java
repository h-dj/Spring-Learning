package cn.hdj.argumentResolver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2021/8/20 11:01
 */
@Configuration
public class CustomConfigurableWebBindingInitializer extends ConfigurableWebBindingInitializer {

    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);

        System.out.println("CustomConfigurableWebBindingInitializer  initBinder");
    }
}
