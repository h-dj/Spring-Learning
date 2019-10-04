# Spring系列之Bean 生命周期分析

<a name="bSZEM"></a>
## 前言
Spring 很复杂，很多东西都很难完全深入了解，这里写下Bean 的初始化的分析，主要是为了解SpringBoot 与Shiro 整合时，引起的事务失败问题，网上很多说是初始化顺序引起没有生成事务代理类而导致失败。为了知其然知其所以然的原则，决定了解了解。 

以下分析基于Spring 5.1.9.RELEASE、shiro1.4.0
<a name="H9PYM"></a>
## 一、Bean 初始化过程
<a name="wIds7"></a>
### 1.  Bean初始化入口：ApplicationContext

  Application 继承结构图<br />![ApplicationContext-Level.png](https://cdn.nlark.com/yuque/0/2019/png/438760/1570029991040-8a0648e7-d8d7-4866-a85f-9d99a68428ac.png#align=left&display=inline&height=728&name=ApplicationContext-Level.png&originHeight=728&originWidth=1134&search=&size=40179&status=done&width=1134)
<a name="7UdcV"></a>
## 
<a name="r6q5E"></a>
### 2. Bean 的生命周期图示
![bean_lifecycle.png](https://cdn.nlark.com/yuque/0/2019/png/438760/1570030172615-92407ee5-9f22-4351-a7e2-f3c8bad7cdbc.png#align=left&display=inline&height=400&name=bean_lifecycle.png&originHeight=400&originWidth=634&search=&size=35606&status=done&width=634)
> 生命周期相关步骤解析：
> 
> 注意： Spring 只帮我们管理单例模式 Bean 的**完整生命周期**，对于 prototype 的 bean ，Spring 在创建好交给使用者之后则不会再管理后续的生命周期。

1. 通过构造函数实例化Bean
1. 依赖注入推荐使用setter 方法(主要注入方法：**setter注入** 、 **constructor构造器注入**)
1. 当依赖注入完成，以下接口方法将会回调：BeanNameAware.setBeanName(String name)；该方法是在Bean Factory 创建bean 时，设置Bean的名称
1.  然后调用：  BeanClassLoaderAware.setBeanClassLoader()， 设置类加载器加载Bean 实例
1. 然后调用： BeanFactoryAware.setBeanFactory(BeanFactory beanFactory) ，为当前的Bean 设置Bean Factory 
1. 容器调用： BeanPostProcessor.postProcessBeforeInitialization，  回调该方法，可将原始bean包装成任意成对像返回
1.  然后如果方法上有注解@PostConstruct  则会被调用（注意：这里需要开启组件扫描，以便对注解进行处理）
1.  在注解@PostConstruct 方法调用后，回调接口方法InitializingBean.afterPropertiesSet() 
1.   调用在xml 配置中bean 标签属性init-method 定义的方法
1. 然后调用 BeanPostProcessor.postProcessAfterInitialization()， 调用该方法，用于包装原始bean 
1.  到目前为止，bean已经初始化，可以使用了
1.  当ApplicationContext 销毁钩子registerShutdownHook() 调用(调用后不是会立即销毁)， 有注解@PreDestroy 等销毁方法会被调用
1.  然后接着调用Bean 实现接口 DisposableBean.destroy() 方法
1. 然后调用在xml 配置中bean 标签属性  destroy-method 的方法
1. 垃圾回收器会自动调用Object 类的finalize() 方法销毁对象
> 初始化回调的方法

1. 接口InitializingBean 的afterPropertiesSet() 方法，Spring 不推荐使用
1. 注解@PostConstruct的方法
1. xml配置中bean标签属性 init-method  指定的方法 或  JavaConfig 注解@Bean  中属性initMethod 指定的方法
1. 如果以上上三种方法都使用了，则按以下顺序执行
  1. 先调用注解@PostConstruct的方法
  1. 再调用 接口InitializingBean 的afterPropertiesSet() 方法
  1. 然后才调用 xml 配置的 init-method 指定方法 或 Java配置中的initMethod 指定的方法
> 销毁回调的方法

1. 接口DisposableBean的 destroy()， Spring 不推荐使用
1. 推荐使用 注解@PreDestroy标识的方法 或 在xml 配置 bean 标签属性destroy-method  定义的方法(Java 配置中，注解@Bean 属性destroyMethod 定义的方法)
1. 如果以上方法都配置，则按以下顺序执行
  1. 注解@PreDestroy 标识的方法先执行
  1. 然后执行在xml 配置 bean 标签属性destroy-method  定义的方法(Java 配置中，注解@Bean 属性destroyMethod 定义的方法)
  1. 最后执行接口DisposableBean的 destroy()
<a name="zY5cg"></a>
## 二、例子
<a name="0xN7l"></a>
### 1. 创建Maven 项目
添加Spring 依赖
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
     <version>5.1.9.RELEASE</version>
</dependency>
```
<a name="dxe9W"></a>
### 2. 创建Student 类，并实现相关接口

```java
public class Student implements InitializingBean, DisposableBean, 
									BeanFactoryAware, BeanNameAware {

    private String name;
    private Integer age;
    public Student() {
        System.out.println("初始化构造函数");
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    @PostConstruct
    public void springPostConstruct() {
        System.out.println("---@PostConstruct--- 执行");
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("---InitializingBean.afterPropertiesSet---");
    }
    public void myInitMethod() {
        System.out.println("---init-method---");
    }
    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
    @Override
    public void setBeanName(String name) {
        System.out.println("---BeanNameAware.setBeanName---");
    }
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("---BeanFactoryAware.setBeanFactory---");
    }
    @PreDestroy
    public void springPreDestroy(){
        System.out.println("-----@PreDestroy-----");
    }
    @Override
    public void destroy() throws Exception {
        System.out.println("-----DisposableBean.destroy()------");
    }
    public void myDestroyMethod(){
        System.out.println("---destroy-method---");
    }
}
```
<a name="PwkEP"></a>
### 3. 创建BeanPostProcessor 自定义实现类

```java
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        System.out.println("BeanPostProcessor.postProcessAfterInitialization");
        return bean;
    }
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        System.out.println("BeanPostProcessor.postProcessBeforeInitialization");
        return bean;
    }
}
```
<a name="TOngM"></a>
### 4. 使用xml配置Bean

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://www.springframework.org/schema/util 
                           https://www.springframework.org/schema/util/spring-util.xsd
                           http://www.springframework.org/schema/context 
                           https://www.springframework.org/schema/context/spring-context.xsd">

    <!--组件扫描-->
    <context:component-scan base-package="cn.hdj"/>
    <!--配置bean,  加载的顺序由上到下，先定义的Bean先初始化-->
    <bean id="student" class="cn.hdj.entity.Student" init-method="myInitMethod" destroy-method="myDestroyMethod"/>
    
    <bean id="myBeanPostProcessor" class="cn.hdj.MyBeanPostProcessor"/>
</beans>
```
<a name="EFvnL"></a>
### 5. 实例化上下文

```java
public static void main(String[] args) {
    AbstractApplicationContext context = new ClassPathXmlApplicationContext("classpath:application-Context.xml");
    Student bean = context.getBean(Student.class);
    System.out.println(bean);
    context.registerShutdownHook();
}
```
<a name="pbyy9"></a>
### 6. 启动项目，输出结果

```shell
初始化构造函数
---BeanNameAware.setBeanName---
---BeanFactoryAware.setBeanFactory---
BeanPostProcessor.postProcessBeforeInitialization
---@PostConstruct--- 执行
---InitializingBean.afterPropertiesSet---
---init-method---
BeanPostProcessor.postProcessAfterInitialization
Student{name='null', age=null}
-----@PreDestroy-----
-----DisposableBean.destroy()------
---destroy-method---
```

以上就是Bean 初始化的过程，如果说我们想要对Bean 初始化过程中，对Bean 做一些处理，那么接下来我们需要了解一下，Spring 为我们提供的扩展点接口。

<a name="SzIYA"></a>
## 三、Aware 接口
> `*Aware` 接口可以用于在初始化 bean 时获得 Spring 中的一些对象，如获取 `Spring 上下文`等。
> 以下为Spring 提供的*Aware接口

 

| Aware 接口 | 实现的方法 | 作用（在Bean 构造函数初始后回调，但在初始化回调方法前调用） |
| --- | --- | --- |
| ApplicationContextAware | void setApplicationContext(ApplicationContext applicationContext) throws BeansException; | Interface to be implemented by any object that wishes to be notified of the ApplicationContext that it runs in. |
| ApplicationEventPublisherAware | void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher); | Set the ApplicationEventPublisher that this object runs in. |
| BeanClassLoaderAware | void setBeanClassLoader (ClassLoader classLoader); | Callback that supplies the bean class loader to a bean instance. |
| BeanFactoryAware | void setBeanFactory (BeanFactory beanFactory) throws BeansException; | Callback that supplies the owning factory to a bean instance. |
| BeanNameAware | void setBeanName(String name); | Set the name of the bean in the bean factory that created this bean. |
| BootstrapContextAware | void setBootstrapContext (BootstrapContext bootstrapContext); | Set the BootstrapContext that this object runs in. |
| LoadTimeWeaverAware | void setLoadTimeWeaver (LoadTimeWeaver loadTimeWeaver); | Set the LoadTimeWeaver of this object’s containing ApplicationContext. |
| MessageSourceAware | void setMessageSource (MessageSource messageSource); | Set the MessageSource that this object runs in. |
| NotificationPublisherAware | void setNotificationPublisher(NotificationPublisher notificationPublisher); | Set the NotificationPublisher instance for the current managed resource instance. |
| PortletConfigAware | void setPortletConfig (PortletConfig portletConfig); | Set the PortletConfig this object runs in. |
| PortletContextAware | void setPortletContext (PortletContext portletContext); | Set the PortletContext that this object runs in. |
| ResourceLoaderAware | void setResourceLoader (ResourceLoader resourceLoader); | Set the ResourceLoader that this object runs in. |
| ServletConfigAware | void setServletConfig (ServletConfig servletConfig); | Set the ServletConfig that this object runs in. |
| ServletContextAware | void setServletContext (ServletContext servletContext); | Set the ServletContext that this object runs in. |

<a name="HvV4V"></a>
## 四、增强处理器
> 种类

1. BeanPostProcessor<br />2. BeanFactoryPostProcessor （不在Bean 生命周期内）
> 增强处理器处理过程


![](https://cdn.nlark.com/yuque/0/2019/png/438760/1570033826324-11e2b579-0f48-4d7c-96c1-05ca578cfc46.png#align=left&display=inline&height=399&originHeight=399&originWidth=616&search=&size=0&status=done&width=616)

增强处理器的特点

- BeanFactoryPostProcessor是加载Bean定义后执行
- BeanPostProcessor 则是在Bean 初始化和依赖注入后执行
- 如果你在一个容器内定义了增强处理器，它就只作用于这个容器内的bean，即使其它容器与该容器有相同的层级关系
- 想要控制多个处理器的处理顺序，可以实现Order 接口来控制，数值越小，优先级越大

BeanFactoryPostProcessor的接口定义

```java
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory);
}
```
BeanPostProcessor 的接口定义

```java
public interface BeanPostProcessor {
    //前置处理
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
    //后置处理
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
```

BeanPostProcessor 的注册过程？

```java
//用于实例化和注册BeanPostProcessor
protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
}

public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// 区分不同优先级的BeanPostProcessor
    	//优先级分别有：PriorityOrdered，Ordered和其他的
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                //实现了接口PriorityOrdered的BeanPostProcessor
                //会优先实例化
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 首先对实现了接口 PriorityOrdered.的BeanPostProcessor进行排序
    	//然后注册
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// 接着对实现了接口 Ordered.的BeanPostProcessor进行排序
    	//然后注册
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// 再接着对普通的BeanPostProcessor进行排序
    	//然后注册
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		//最后对内部定义的BeanPostProcessor进行排序，然后注册
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		//重新注册用于将内部bean检测为ApplicationListener的后处理器，
		//将其移到处理器链的末尾（for picking up proxies etc）。
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}
```

> BeanPostProcessor按优先级分为PriorityOrdered，Ordered和其他的，对他们分别进行以下操作。
> - 先beanFactory.getBean进行实例化，
> - 再使用sortPostProcessors() 进行排序
> - 最后registerBeanPostProcessors()进行注册。


所以，优先级高Bpp的先被实例化，而优先级低的Bpp在实例化时可以应用优先级高的(不过同级或优先级低的不行，因为还没初始化)


那问题来了，BeanPostProcessor依赖的Bean,　导致其提前初始化，有什么后果？<br />根据BeanPostProcessor 的注册过程，我们可以知道如果Bean 被优先级高的Bpp依赖而导致提前初始化，那么这个Bean 就不会在被其他优先级低的Bpp处理（Bpp还没初始化）；　例如典型的例子就是　auto-proxy，因为<br />AutowiredAnnotationBeanPostProcessor(用于处理依赖注入的Bpp)的优先级是PriorityOrdered，　而AnnotationAwareAspectJAutoProxyCreator(用于 auto-proxy)的优先级是Ordered，　如果Bean 在AnnotationAwareAspectJAutoProxyCreator注册前实例化，那么这个Bean依赖注入时，就不会以代理类(JDK代理或CGlib代理)的形式注入

解决的方法： 　1. 对依赖的Bean 添加@Lazy(懒加载注解)，2.  使用注解@DependsOn

<a name="3rxKm"></a>
## 五、Spring 整合Shiro 引起事务失败问题解答
<a name="ujduN"></a>
### 1.问题原因

-  在DbShiroRealm 中注入UserService时，未加懒加载注解@Lazy ，打印的Bean 实例化顺序

![image.png](https://cdn.nlark.com/yuque/0/2019/png/438760/1570195909063-41c4c383-1f6d-4afb-bfc5-53748bc13674.png#align=left&display=inline&height=251&name=image.png&originHeight=502&originWidth=1100&search=&size=95370&status=done&width=550)

- Shiro 的配置

```java
@Configuration
public class ShiroConfig {

    /**
     * 验证过滤器
     *
     * @return
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        JwtAuthFilter authFilter = new JwtAuthFilter();
        return authFilter;
    }

    /**
     * 不加入Servlet Filter中
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(jwtAuthFilter());
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }


    @Bean
    public Realm dbShiroRealm() {
        DbShiroRealm myShiroRealm = new DbShiroRealm();
        myShiroRealm.setCredentialsMatcher(new HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME));
        return myShiroRealm;
    }

    @Bean
    public Realm jwtShiroRealm() {
        JwtShiroRealm myShiroRealm = new JwtShiroRealm();
        myShiroRealm.setCredentialsMatcher(new JwtCredentialsMatcher());
        return myShiroRealm;
    }


    /**
     * 关闭session
     *
     * @return
     */
    @Bean
    public SessionStorageEvaluator sessionStorageEvaluator() {
        DefaultWebSessionStorageEvaluator sessionStorageEvaluator = new DefaultWebSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        return sessionStorageEvaluator;
    }

    /**
     * 验证器
     *
     * @return
     */
    @Bean
    public Authenticator authenticator() {
        MyModularRealmAuthenticator authenticator = new MyModularRealmAuthenticator();
        return authenticator;
    }

    /**
     * 设置过滤器链
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {

        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        //设置登陆的url
        factoryBean.setLoginUrl("/api/admin/user/signIn");


        factoryBean.setSecurityManager(securityManager);
        //设置过滤器
        Map<String, Filter> filters = factoryBean.getFilters();
        filters.put("authToken", jwtAuthFilter());
        factoryBean.setFilters(filters);


        // 自定义url规则使用LinkedHashMap有序Map
        Map<String, String> filterMap = new LinkedHashMap<>();
        // 两个url规则都可以匹配同一个url，只执行第一个
        filterMap.put("/api/admin/user/signIn", "anon");
        filterMap.put("/api/admin/**", "authToken");
        factoryBean.setFilterChainDefinitionMap(filterMap);
        return factoryBean;
    }
}

```

- 说明
1. ShiroFilterFactoryBean 是FactoryBean，且实现了BeanPostProcessor接口
1. 在实例化时，依赖securityManager
1. 而SecurityManager 在Shiro自动化配置中，又依赖于Realm 类的
1. 所以实例化的顺序为：ShiroFilterFactoryBean > securityManager > dbShiroRealm > userServiceImpl
1. 此时代理事务配置 ProxyTransactionManagementConfiguration还没实例化 

<a name="dZarE"></a>
### ２.解决方法

1. 采用懒加载的方式

```java
    @Lazy
    @Autowired
    private UserService userService;	
```

2. 使用注解@DependsOn

说明：<br />既然我们再创建dbShiroRealm时，需要依赖注入userServiceImpl而且希望注入的是事务代理增强类，但是事务代理增强的配置在 ProxyTransactionManagementConfiguration类中，那么我们在实例化dbShiroRealm时，先让配置类实例化，这样就可以享受到事务代理了。

```java
//@DependsOn中的Bean 名称可以在ProxyTransactionManagementConfiguration中找
@Bean
@DependsOn("org.springframework.transaction.config.internalTransactionAdvisor")
public Realm dbShiroRealm() {
    DbShiroRealm myShiroRealm = new DbShiroRealm();
    myShiroRealm.setCredentialsMatcher(new HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME));
    return myShiroRealm;
}
```


<a name="WqGTh"></a>
## 六 、参考

- [https://zhuanlan.zhihu.com/p/30112785](https://zhuanlan.zhihu.com/p/30112785) （bean 初始化顺序）
- [https://www.concretepage.com/spring/spring-bean-life-cycle-tutorial](https://www.concretepage.com/spring/spring-bean-life-cycle-tutorial)
- [https://juejin.im/post/5ab30714f265da237b21fbcc](https://juejin.im/post/5ab30714f265da237b21fbcc)
- [https://blog.csdn.net/weixin_43364172/article/details/84630770](https://blog.csdn.net/weixin_43364172/article/details/84630770)
- [https://www.iteye.com/blog/jinnianshilongnian-1894973](https://www.iteye.com/blog/jinnianshilongnian-1894973)
- [https://blog.csdn.net/finalcola/article/details/81197584](https://blog.csdn.net/finalcola/article/details/81197584)
- [https://blog.csdn.net/f641385712/article/details/89737791](https://blog.csdn.net/f641385712/article/details/89737791)
