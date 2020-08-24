package cn.hdj.controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/24 下午4:09
 * @description:
 */
@WebServlet("/hello")
public class HelloWorldController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("初始化！！！");
        PrintWriter writer = resp.getWriter();
        writer.println("132");
    }
}
