package cn.hdj.security.config;

import cn.hdj.common.api.ResultVO;
import cn.hdj.modules.user.service.ISysPermissionService;
import cn.hdj.modules.user.service.ISysUserService;
import cn.hdj.security.core.DynamicAuthService;
import cn.hdj.security.core.JwtAuthenticationTokenFilter;
import cn.hdj.security.core.userdetails.user.SysUserDetailServiceImpl;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author huangjiajian
 * @Date 2022/6/26 上午12:01
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig {


    @Lazy
    @Autowired
    private ISysUserService sysUserService;
    @Lazy
    @Autowired
    private ISysPermissionService sysPermissionService;

    /**
     * 后台用户查询服务
     *
     * @return
     */
    @Bean(name = "sysUserDetailsService")
    public UserDetailsService sysUserDetailsService() {
        return new SysUserDetailServiceImpl(sysUserService);
    }


    /**
     * 密码编码器
     * <p>
     * 委托方式，根据密码的前缀选择对应的encoder，例如：{bcypt}前缀->标识BCYPT算法加密；{noop}->标识不使用任何加密即明文的方式
     * 密码判读 DaoAuthenticationProvider#additionalAuthenticationChecks
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    /**
     * 用户名密码认证授权提供者
     *
     * @return
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(sysUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        //是否隐藏用户不存在异常，默认:true-隐藏；false-抛出异常；
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    /**
     * 暴露 AuthenticationManager
     *
     * @param authenticationConfiguration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    /**
     * 认证失败处理
     *
     * @return
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedHandler() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                log.error(authException.getMessage(), authException);
                log.error("url --->{}", request.getRequestURI());
                ServletUtil.write(response, JSONUtil.toJsonStr(ResultVO.errorJson(authException.getMessage(), 400)), ContentType.JSON.getValue());
            }
        };
    }

    /**
     * 授权失败处理器
     *
     * @return
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                log.error(accessDeniedException.getMessage(), accessDeniedException);
                log.error("url --->{}", request.getRequestURI());
                ServletUtil.write(response, JSONUtil.toJsonStr(ResultVO.errorJson(accessDeniedException.getMessage(), 400)), ContentType.JSON.getValue());
            }
        };
    }


    /**
     * jwt token 验证
     *
     * @return
     */
    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
        return new JwtAuthenticationTokenFilter();
    }

    /**
     * 动态权限判断
     *
     * @return
     */
    @Bean(name = "da")
    public DynamicAuthService dynamicAuthService() {
        return new DynamicAuthService(sysPermissionService);
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //请求认证
        http.authorizeRequests()
                // 静态资源，可匿名访问
                .mvcMatchers("/webjars/**", "/doc.html", "/swagger-resources/**", "/v2/api-docs").permitAll()
                // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                .mvcMatchers("/oauth/**", "/sms-code").permitAll()
                //跨域
                .mvcMatchers(HttpMethod.OPTIONS).permitAll()
                .mvcMatchers("/**").access("@da.check(authentication,request)");


        http
                //跨域
                .cors()
                .and()
                // CSRF禁用，因为不使用session
                .csrf()
                .disable()
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler())
                //授权失败处理类
                .accessDeniedHandler(accessDeniedHandler())
                .and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers().frameOptions().disable();


        //添加JWT filter
        http.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
