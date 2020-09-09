package cn.hdj.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/24 下午4:55
 * @description: 登录控制器
 */
@WebServlet("/login")
public class LoginController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取用户名，密码
        String json = IoUtil.read(req.getInputStream(), "utf-8");
        Map map = JSONUtil.toBean(json, Map.class);
        String username = (String) map.get("username");
        String password = (String) map.get("password");


    }
}
