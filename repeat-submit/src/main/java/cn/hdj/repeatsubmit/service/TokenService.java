package cn.hdj.repeatsubmit.service;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author cloud-inman
 * @Date 2021/12/15 19:19
 */
public interface TokenService {

    /**
     * 生成token
     * @return
     */
    String createToken();


    /**
     * 校验token是否合法
     * @param request httpRequest对象
     * @return true：合法; false:不合法
     */
    boolean checkToken(HttpServletRequest request);
}
