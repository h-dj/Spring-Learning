package cn.hdj.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import java.io.IOException;

/**
 * @author hdj
 * @Description:　注解式
 * @date 9/27/19
 */
@WebFilter(
        //初始化参数
        //@WebInitParam
        initParams = {
                @WebInitParam(name = "name", value = "HelloWorld")
        },
        urlPatterns = "/*"
)
public class AnnotationHelloFilter implements Filter {



    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        System.out.println("AnnotationHelloFilter　执行过滤; ");
        System.out.println("初始化参数" + this.filterConfig.getInitParameter("name"));
        //执行下一个
        chain.doFilter(request, response);
    }

    public void destroy() {
    }
}
