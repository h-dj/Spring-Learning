package cn.hdj.oom.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @Author huangjiajian
 * @Date 2022/8/13 下午6:32
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@WebFilter(
        urlPatterns = "/*"
)
@Slf4j
public class SecurityFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("enter SecurityFilter {}", request.getRequestURI());

        // session 内存泄漏
        if (request.getRequestURI().contains("/api/user")) {
            HttpSession session = request.getSession();
        }

        filterChain.doFilter(request, response);
    }
}
