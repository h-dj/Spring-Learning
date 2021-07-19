package cn.hdj.cors.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.addHeader("Access-Control-Max-Age", "3600");
        response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, CMALL-TOKEN");
        response.addHeader("Access-Control-Allow-Credentials", "true"); // cookie


        String method = request.getMethod();
        String origin = request.getHeader("Origin");
        response.addHeader("Access-Control-Allow-Origin", origin);
//        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(method)
//                && request.getHeader(HttpHeaders.ORIGIN) != null
//                && request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null) {
//            return;
//        }

        filterChain.doFilter(request, response);
    }

}
