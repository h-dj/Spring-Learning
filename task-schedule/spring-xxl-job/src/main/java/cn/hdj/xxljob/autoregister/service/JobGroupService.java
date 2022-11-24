package cn.hdj.xxljob.autoregister.service;

import cn.hdj.xxljob.autoregister.model.XxlJobGroup;
import cn.hdj.xxljob.config.XxlJobProperties;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.cookie.GlobalCookieManager;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.CookieManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/11/23 17:56
 */
@Service
@Slf4j
public class JobGroupService {

    @Autowired
    private XxlJobProperties xxlJobProperties;

    public List<XxlJobGroup> getJobGroup() {

        String url = xxlJobProperties.getAdmin().getAddresses() + "/jobgroup/pageList";
        HttpResponse response = HttpRequest.post(url)
                .form("appname", xxlJobProperties.getExecutor().getAppname())
                .form("title", xxlJobProperties.getExecutor().getTitle())
                .execute();

        String body = response.body();
        JSONArray array = JSONUtil.parse(body).getByPath("data", JSONArray.class);
        List<XxlJobGroup> list = array.stream()
                .map(o -> JSONUtil.toBean((JSONObject) o, XxlJobGroup.class))
                .collect(Collectors.toList());

        return list;
    }

    public boolean preciselyCheck() {
        List<XxlJobGroup> jobGroup = getJobGroup();
        Optional<XxlJobGroup> has = jobGroup.stream()
                .filter(xxlJobGroup -> xxlJobGroup.getAppname().equals(xxlJobProperties.getExecutor().getAppname())
                        && xxlJobGroup.getTitle().equals(xxlJobProperties.getExecutor().getTitle()))
                .findAny();
        return has.isPresent();
    }

    public boolean autoRegisterGroup() {
        String url = this.xxlJobProperties.getAdmin().getAddresses() + "/jobgroup/save";
        HttpRequest httpRequest = HttpRequest.post(url)
                .form("appname", xxlJobProperties.getExecutor().getAppname())
                .form("title", xxlJobProperties.getExecutor().getTitle());

        httpRequest.form("addressType", 0);

        HttpResponse response = httpRequest.execute();
        Object code = JSONUtil.parse(response.body()).getByPath("code");
        return ObjectUtil.equal(code,200);
    }

    public void autoRegister() {
        if (this.preciselyCheck())
            return;

        if (this.autoRegisterGroup())
            log.info("auto register xxl-job group success!");

    }
}
