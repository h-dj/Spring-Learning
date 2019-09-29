package cn.hdj.mvninterceptor.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author hdj
 * @Description:
 * @date 9/29/19
 */
public class SecondFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("SecondFilter.init");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("SecondFilter.doFilter");
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {
        System.out.println("SecondFilter.destroy");
    }
}
