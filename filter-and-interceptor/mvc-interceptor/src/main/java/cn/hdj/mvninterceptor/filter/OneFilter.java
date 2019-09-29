package cn.hdj.mvninterceptor.filter;

import cn.hdj.mvninterceptor.controller.HelloController;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author hdj
 * @Description:
 * @date 9/29/19
 */
public class OneFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("OneFilter.init");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("OneFilter.doFilter");
        //执行下一个filter
        filterChain.doFilter(servletRequest, servletResponse);

    }

    @Override
    public void destroy() {
        System.out.println("OneFilter.destroy");
    }
}
