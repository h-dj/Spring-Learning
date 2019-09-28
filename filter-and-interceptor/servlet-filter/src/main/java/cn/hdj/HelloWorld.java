package cn.hdj;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author hdj
 * @Description:
 * @date 9/27/19
 */
public class HelloWorld extends HttpServlet {



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("Servlet名称: name=" + this.getInitParameter("name") +" HelloWorld　doGet");
        PrintWriter writer = resp.getWriter();
        writer.println("Servlet名称: name=" + this.getInitParameter("name") +" HelloWorld　doGet");
        writer.flush();
        writer.close();

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Servlet名称: name=" + this.getInitParameter("name") +" HelloWorld　doPost");
        PrintWriter writer = resp.getWriter();
        writer.println("Servlet名称: name=" + this.getInitParameter("name") +" HelloWorld　doGet");
        writer.flush();
        writer.close();


    }
}
