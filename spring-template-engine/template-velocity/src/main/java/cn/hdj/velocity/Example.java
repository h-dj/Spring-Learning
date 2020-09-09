package cn.hdj.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/9/9 下午10:10
 * @description: velocity 语法学习
 */
public class Example {

    public static void main(String[] args) {
        /* 首先，初始化运行时引擎，使用默认的配置 */

        Velocity.setProperty("file.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        /* 创建Context对象，然后把数据放进去 */
        VelocityContext context = new VelocityContext();

        //字符串
        context.put("name", "Velocity");
        context.put("project", "Jakarta");

        //基本类型

        context.put("int", 123);
        context.put("double", 123.123);


        //对象
        Map<String, String> map = new HashMap<>();
        map.put("name", "Java");
        map.put("age", "25");

        context.put("obj", map);

        //数组
        Integer[] arr = {1, 2, 9, 4, null};
        context.put("arr", arr);

        /* 渲染模板 */
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate("template.vm", "utf-8", context, w);
        System.out.println(" template : " + w);
    }
}
