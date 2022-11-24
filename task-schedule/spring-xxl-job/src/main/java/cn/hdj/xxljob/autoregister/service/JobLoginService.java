package cn.hdj.xxljob.autoregister.service;

import cn.hdj.xxljob.config.XxlJobProperties;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.cookie.GlobalCookieManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/11/24 9:18
 */
@Service
@Slf4j
public class JobLoginService {


    @Autowired
    private XxlJobProperties xxlJobProperties;


    /**
     * 登录
     */
    public void login() throws IOException {
        String url = xxlJobProperties.getAdmin().getAddresses() + "/login";

        List<HttpCookie> cookieList = GlobalCookieManager
                .getCookieManager()
                .getCookieStore()
                .get(URLUtil.toURI(url));

        if (CollectionUtil.isNotEmpty(cookieList)) {
            Map<String, String> cookieMap = cookieList.stream()
                    .collect(Collectors.toMap(HttpCookie::getName, HttpCookie::getValue));
            if (cookieMap.get(XxlJobProperties.LOGIN_IDENTITY_KEY) != null) {
                log.info("xxl-job login successfully!");
                return;
            }
        }

        HttpResponse response = HttpRequest.post(url)
                .form("userName", xxlJobProperties.getAdmin().getUsername())
                .form("password", xxlJobProperties.getAdmin().getPassword())
                .execute();

        List<HttpCookie> cookies = response.getCookies();
        Optional<HttpCookie> cookieOpt = cookies.stream()
                .filter(cookie -> cookie.getName().equals("XXL_JOB_LOGIN_IDENTITY")).findFirst();
        if (!cookieOpt.isPresent()) {
            throw new RuntimeException("get xxl-job cookie error!");
        }
    }

}
