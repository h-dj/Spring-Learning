package cn.hdj.mvc.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/6/15 17:09
 */
public class MyWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {


    /**
     * Spring Root上下文配置
     *
     * @return
     */
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{RootConfig.class};
    }


    /**
     * SpringMVC 配置
     *
     * @return
     */
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebMvcConfig.class};
    }

    /**
     * MVC 映射路径
     *
     * @return
     */
    protected String[] getServletMappings() {
        return new String[]{
                "/*"
        };
    }
}
