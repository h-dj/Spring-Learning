package cn.hdj.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author hdj
 * @Description:
 * @date 9/27/19
 */
public class HelloFilter implements Filter {

    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        System.out.println("HelloFilter　执行过滤");
        //执行下一个
        chain.doFilter(request, response);
    }

    public void destroy() {
    }
}
