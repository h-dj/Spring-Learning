package cn.hdj.security.config;

import cn.hdj.common.api.ResultVO;
import cn.hdj.modules.user.service.ISysPermissionService;
import cn.hdj.modules.user.service.ISysUserService;
import cn.hdj.security.core.DynamicAuthService;
import cn.hdj.security.core.JwtAuthenticationTokenFilter;
import cn.hdj.security.core.userdetails.user.SysUserDetailServiceImpl;
import cn.hdj.security.core.userdetails.user.SysUserSmsDetailServiceImpl;
import cn.hdj.security.extension.mobile.SmsCodeAuthenticationProvider;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author huangjiajian
 * @Date 2022/6/26 上午12:01
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Resource(name = "sysUserDetailsService")
    private UserDetailsService sysUserDetailsService;
    @Resource(name = "sysUserSmsDetailsService")
    private UserDetailsService sysUserSmsDetailsService;


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
        provider.setUserDetailsService(sysUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        //是否隐藏用户不存在异常，默认:true-隐藏；false-抛出异常；
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }


    /**
     * 手机短信验证码登录认证授权提供者
     *
     * @return
     */
    @Bean
    public SmsCodeAuthenticationProvider smsCodeAuthenticationProvider() {
        SmsCodeAuthenticationProvider provider = new SmsCodeAuthenticationProvider();
        provider.setUserDetailsService(sysUserSmsDetailsService);
        provider.setRedissonClient(redissonClient);
        return provider;
    }

    /**
     * 暴露 AuthenticationManager
     *
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(ListUtil.of(
                daoAuthenticationProvider(),
                smsCodeAuthenticationProvider()
        ));
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


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //请求认证
        http
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler())
                //授权失败处理类
                .accessDeniedHandler(accessDeniedHandler())
                .and()
                .authorizeRequests()
                // 静态资源，可匿名访问
                .mvcMatchers("/webjars/**", "/doc.html", "/swagger-resources/**", "/v2/api-docs").permitAll()
                // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                .mvcMatchers("/oauth/**", "/test/**").permitAll()
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
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers().frameOptions().disable();


        //添加JWT filter
        http.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
