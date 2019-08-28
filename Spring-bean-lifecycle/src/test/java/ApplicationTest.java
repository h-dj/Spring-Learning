import cn.hdj.entity.Student;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author hdj
 * @Description:
 * @date 8/21/19
 */
public class ApplicationTest {

    public static void main(String[] args) throws InterruptedException {

        AbstractApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:application-Context.xml"});
        Student bean = context.getBean(Student.class);
        System.out.println(bean);
        context.registerShutdownHook();

    }

}
