### 一、　Filter 过滤器
##### 1. 什么是Filter 过滤器?
* Filter 的含义
 Filter 过滤器是一个对象 ，它对请求资源（Servlet或静态内容），或者来自资源的响应，或两者执行过滤任务。
* Filter 的过滤任务执行流程
![filter.png](https://upload-images.jianshu.io/upload_images/4840929-5380917a26f0336e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* Filter 的生命周期(来自google)
![Filter lifecycle.png](https://upload-images.jianshu.io/upload_images/4840929-1aa4cf76f3abae2b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 过滤器特点
  1. Servlet过滤器可能检查和修改ServletRequest和ServletResponse对象
  2. 可以指定Servlet过滤器和特定的URL关联，只有当客户请求访问此URL时，才会触发该过滤器工作
  3. 多个Servlet过滤器可以被串联起来，形成管道效应，协同修改请求和响应对象
  4. 所有支持Java Servlet规范2.3的Servlet容器，都支持Servlet过滤器
* Filter 的应用场景
  1. 认证过滤器
  2. 记录和审核过滤器
  3. 图像转换滤镜
  4. 数据压缩过滤器
  5. 加密过滤器
  6. 标记化过滤器
  7. 触发资源访问事件的过滤器
  8.  Mime-type 过滤器
* Filter 类型
  1. REQUEST：默认值，代表直接访问某个资源时执行filter
  2. FORWARD：转发时才执行filter
  3. INCLUDE: 包含资源时执行filter
  4. ERROR：发生错误时 进行跳转时执行filter
* 接口定义
```
public interface Filter {

     /**
     * 这是Servlet过滤器的初始化方法，Servlet容器创建Servlet过滤器实例后就会调用这个方法。
     * 在这个方法中可以通过FilterConfig来读取web.xml文件中Servlet过滤器的初始化参数。
     *　注意：在Filter 被创建到销毁，只会执行　init　方法一次
     *　＠param FilterConfig : Filter 配置参数
     *                   public String getFilterName();//获取过滤器名称
     *                   public ServletContext getServletContext();//获取Servlet容器
     *                  public String getInitParameter(String name);//获取初始化参数
     *                  public Enumeration<String> getInitParameterNames();//获取全部的初始化参数
     */
    public default void init(FilterConfig filterConfig) throws ServletException {}


     /**
     *　这是完成实际的过滤操作的方法，当客户请求访问与过滤器关联的URL时，Servlet容器先调用该方法。
     * FilterChain参数用来访问后续的过滤器的doFilter()方法。
     * @param request  请求
     * @param response 响应
     * @param chain    Filter过滤链对象
     *
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException;

    /**
     * Servlet容器在销毁过滤器实例前调用该方法，在这个方法中，可以释放过滤器占用的资源。
     */
    public default void destroy() {}
}
```
#####  2. 如何定义Filter
* 第一步：定义Filter
```
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
```
* 第二步：注册到FilterChain中
      1. 在web.xml中注册
```
<filter>
        <filter-name>HelloFilter</filter-name>
        <filter-class>cn.hdj.filter.HelloFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>HelloFilter</filter-name>
        <!--
            url 规则定义:
            /* 匹配所有请求链接
            /user/*  匹配/user/路径所有请求链接
            *.extension  匹配以extension为后缀的请求
        -->
        <url-pattern>/*</url-pattern>
        <!-- Filter 类型-->
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>
```
  2. Servlet 3.0后新增了注解（@WebFilter）支持
不用在web.xml中定义，Spring 项目需要添加注解@ServletComponentScan，用于扫描Servlet3.0 支持的@WebFilter, @WebServlet, @WebListener等注解，加入IOC容器
  ```
@WebFilter(
        //初始化参数
        //@WebInitParam
        initParams = {
                @WebInitParam(name = "name", value = "HelloWorld")
        },
        urlPatterns = "/*",
        dispatcherTypes = DispatcherType.REQUEST,
         //异步支持
        asyncSupported = true
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
```
  3. 在Spring 中 使用 FilterRegistrationBean 注册
```
//把过滤器交给IOC 容器管理
@Bean
public OneFilter oneFilter() {
    return new OneFilter();
}
/**
 * 注册过滤器到Servlet FilterChain中
 */
@Bean
public FilterRegistrationBean FilterRegistrationBean(OneFilter oneFilter) {
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
    filterRegistrationBean.setFilter(oneFilter);
    //设置true 自动注册到Servlet 过滤链中，
    //false ，则该Filter 不会添加到过滤器链中,只是普通Bean
    //具体源码看(SpringBoot 2.1.8)：（其中还有很多步骤，主要顺序是这三个步骤）
    //1. SpringApplication.run(Class<?> primarySource, String... args)
    //...
    //2. ServletWebServerApplicationContext.refresh()
    //...
    //3. RegistrationBean(FilterRegistrationBean 的父类) onStartup(ServletContext servletContext)
    filterRegistrationBean.setEnabled(true); 
    filterRegistrationBean.addUrlPatterns("/*");//拦截的请求
    filterRegistrationBean.setOrder(1);//执行顺序
    return filterRegistrationBean;
}
```
### 二、Interceptor 拦截器
##### 1. 什么是Interceptor ?
*  了解Interceptor 需要了解SpringMVC 是如何处理请求的？　看如下图 SpringMVC 处理请求流程(来源google)
![Interceptor.png](https://upload-images.jianshu.io/upload_images/4840929-5176eb6616a1b530.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 拦截器的处理过程（包括多个）
![interceptor chain1.png](https://upload-images.jianshu.io/upload_images/4840929-9f1155f1432220df.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![interceptor chain2.png](https://upload-images.jianshu.io/upload_images/4840929-25c9a652d0e6b9f4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
*  从以上的图示中可以看出，　Interceptor 拦截器用来在处理Controller 时进行相关拦截操作，如检查授权、参数验证等。
##### 2. 如何使用Interceptor ?
*  拦截器接口HandlerInterceptor　
```
public interface HandlerInterceptor {
    //预处理，在HandlerMapping　找到相应处理的Controller 后执行，但在HandlerAdapter 执行Controller 前调用
   default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
         throws Exception {
      return true;
   }
   //在执行controller 之后，但在视图渲染前执行
   default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
         @Nullable ModelAndView modelAndView) throws Exception {
   }
   //在请求处理完成后，即视图渲染完后执行
//注意： 只有preHandle方法处理完成并返回true 才会执行afterCompletion方法
   default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
         @Nullable Exception ex) throws Exception {
   }
}
```
* 实现HandlerInterceptor　接口，自定义拦截器
```
public class LogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("LogInterceptor.preHandle ==> url:" + request.getRequestURI());
        return true;
    }
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("LogInterceptor.postHandle ==> handler:" + handler.toString());
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("LogInterceptor.afterCompletion");
    }
}
```
* 注册
```
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

   @Bean
    public LogInterceptor logInterceptor(){
        return new LogInterceptor();
    }
    /**
     * 添加自定义拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor())//添加拦截器, 这里实例化bean 可以由Spring 管理
                .addPathPatterns("/*") //拦截uri
                .order(1); //执行顺序
    }
}
```
### 三、Filter 和Interceptor 的区别
##### 1.  使用范围不同：
> Filter是Servlet规范规定的，只能用于Web程序中。而拦截器既可以用于Web程序，也可以用于Application、Swing程序中。
##### 2.  规范不同：
> Filter是在Servlet规范中定义的，是Servlet容器支持的。而拦截器是在Spring容器内的，是Spring框架支持的。
##### 3. 操作资源不同：
> Filter在过滤是只能对request和response进行操作，而interceptor可以对request、response、handler、modelAndView、exception进行操作。
##### 4. 执行顺序不同:
![Filter_Interceptor.png](https://upload-images.jianshu.io/upload_images/4840929-85ba0754a6b75f56.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 四、参考
1.[http://www.mkjava.com/tutorial/filter-vs-interceptor/](http://www.mkjava.com/tutorial/filter-vs-interceptor/)
2.[https://tomcat.apache.org/tomcat-8.0-doc/servletapi/javax/servlet/Filter.html](https://tomcat.apache.org/tomcat-8.0-doc/servletapi/javax/servlet/Filter.html)
3.[https://www.iteye.com/blog/jinnianshilongnian-1670856](https://www.iteye.com/blog/jinnianshilongnian-1670856)
4.[https://o7planning.org/en/11229/spring-mvc-interceptors-tutorial#a4748118](https://o7planning.org/en/11229/spring-mvc-interceptors-tutorial#a4748118)
5.[https://ixyzero.com/blog/archives/3855.html](https://ixyzero.com/blog/archives/3855.html) 
6.[拦截器机制——《跟我学Shiro》](https://www.iteye.com/blog/jinnianshilongnian-2025656)

### 五、源码
[https://github.com/h-dj/Spring-Learning](https://github.com/h-dj/Spring-Learning)
