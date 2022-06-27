package cn.hdj.mvc;


import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class MvcJavaConfigApplication {

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        Server server = tomcat.getServer();
        Service service = tomcat.getService();
        service.setName("Tomcat-embbeded");
        Connector connector = new Connector("HTTP/1.1");
        connector.setPort(8080);
        service.addConnector(connector);
        tomcat.addWebapp("", System.getProperty("user.dir") + File.separator);
        server.start();
        System.out.println("tomcat服务器启动成功..");
        System.out.println("http://127.0.0.1:8080");
        server.await();
    }
}
