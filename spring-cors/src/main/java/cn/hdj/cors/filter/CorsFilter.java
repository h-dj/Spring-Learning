package cn.hdj.cors.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description: 跨域过滤器
 * @Author huangjiajian
 * @Date 2021/4/18 上午12:10
 */
public class CorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, CMALL-TOKEN");
        response.setHeader("Access-Control-Allow-Credentials", "true"); // cookie
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin);

//        String method = request.getMethod();
//        if ("OPTIONS".equals(method) && origin != null) {
//            return;
//        }

        filterChain.doFilter(request, response);
    }

}
