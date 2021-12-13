package cn.hdj.repeatsubmit.utils;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2021/12/9 20:31
 */
public class SpringElUtil {


    private final static ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>(64);
    private final static ExpressionParser parser = new SpelExpressionParser();

    private SpringElUtil() {
    }

    public static <T> SpringElBuilder<T> createElBuilder() {
        return new SpringElBuilder<>();
    }


    public static class SpringElBuilder<T> {
        /**
         * 方法参数
         */
        private Map<String, Object> argMap;
        private BeanFactory beanFactory;
        private Class<T> target;
        private String keyExpression;


        public SpringElBuilder<T> setArgMap(Map<String, Object> argMap) {
            this.argMap = argMap;
            return this;
        }

        public SpringElBuilder<T> putArgMap(String key, Object value) {
            if (argMap == null) {
                argMap = new HashMap<>(16);
            }
            argMap.put(key, value);
            return this;
        }

        public SpringElBuilder<T> setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
            return this;
        }

        public SpringElBuilder<T> setTarget(Class<T> target) {
            this.target = target;
            return this;
        }

        public SpringElBuilder<T> setKeyExpression(String keyExpression) {
            this.keyExpression = keyExpression;
            return this;
        }

        public T build() {
            Expression expression = expressionCache.get(this.keyExpression);
            if (expression == null) {
                expression = parser.parseExpression(this.keyExpression);
                expressionCache.putIfAbsent(this.keyExpression, expression);
            }
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(this.argMap);
            context.setBeanResolver(new BeanFactoryResolver(this.beanFactory));
            return expression.getValue(context, this.target);
        }
    }

    /**
     * 过滤参数
     *
     * @param arg
     * @return
     */
    private static boolean filterArgs(Object arg) {
        if (arg == null) {
            return false;
        }
        boolean flag = arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof Model
                || arg instanceof ModelMap
                || arg instanceof Filter
                || arg.getClass().getAnnotation(PathVariable.class) != null;

        return !flag;
    }

    public static Map<String, Object> getArgMap(JoinPoint joinPoint) {

        Map<String, Object> argMap = new HashMap<>(16);
        // 获取目标方法
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;

        Object[] args = joinPoint.getArgs();
        String[] parameterNames = methodSignature.getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            Object param = args[i];
            if (filterArgs(param)) {
                argMap.put(parameterNames[i], param);
            }
        }
        return argMap;
    }
}
