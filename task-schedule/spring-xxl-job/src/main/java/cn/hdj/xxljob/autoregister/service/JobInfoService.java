package cn.hdj.xxljob.autoregister.service;

import cn.hdj.xxljob.autoregister.JobAutoRegisterMarker;
import cn.hdj.xxljob.autoregister.annotation.XxlRegister;
import cn.hdj.xxljob.autoregister.model.XxlJobGroup;
import cn.hdj.xxljob.autoregister.model.XxlJobInfo;
import cn.hdj.xxljob.config.XxlJobProperties;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/11/24 10:34
 */
@Slf4j
@Service
public class JobInfoService {

    @Autowired
    private JobGroupService jobGroupService;

    @Autowired
    private XxlJobProperties xxlJobProperties;

    public List<XxlJobInfo> getJobInfo(Integer jobGroupId, String executorHandler) {
        String url = this.xxlJobProperties.getAdmin().getAddresses() + "/jobinfo/pageList";
        HttpResponse response = HttpRequest.post(url)
                .form("jobGroup", jobGroupId)
                .form("executorHandler", executorHandler)
                .form("triggerStatus", -1)
                .execute();
        String body = response.body();
        JSONArray array = JSONUtil.parse(body).getByPath("data", JSONArray.class);
        List<XxlJobInfo> list = array.stream()
                .map(o -> JSONUtil.toBean((JSONObject) o, XxlJobInfo.class))
                .collect(Collectors.toList());
        return list;
    }

    public Integer addJobInfo(XxlJobInfo xxlJobInfo) {
        String url = this.xxlJobProperties.getAdmin().getAddresses() + "/jobinfo/add";
        Map<String, Object> paramMap = BeanUtil.beanToMap(xxlJobInfo);
        HttpResponse response = HttpRequest.post(url)
                .form(paramMap)
                .execute();

        JSON json = JSONUtil.parse(response.body());
        Object code = json.getByPath("code");
        if (code.equals(200)) {
            return Convert.toInt(json.getByPath("content"));
        }
        throw new RuntimeException("add jobInfo error!");
    }


    public void addJobInfo() {
        List<XxlJobGroup> jobGroups = jobGroupService.getJobGroup();
        XxlJobGroup xxlJobGroup = jobGroups.get(0);


        Map<String, JobAutoRegisterMarker> jobAutoRegisterMarkerMap = SpringUtil.getBeansOfType(JobAutoRegisterMarker.class);


        for (Map.Entry<String, JobAutoRegisterMarker> entry : jobAutoRegisterMarkerMap.entrySet()) {
            JobAutoRegisterMarker registerMarker = entry.getValue();

            //获取声明的方法
            Method[] methods = ReflectUtil.getMethods(registerMarker.getClass(), method -> AnnotationUtil.getAnnotation(method, XxlRegister.class) != null);

            for (Method method : methods) {
                XxlRegister xxlRegister = AnnotationUtil.getAnnotation(method, XxlRegister.class);
                XxlJob xxlJob = AnnotationUtil.getAnnotation(method, XxlJob.class);

                //查询任务详情
                List<XxlJobInfo> jobInfo = this.getJobInfo(xxlJobGroup.getId(), xxlJob.value());
                if (CollectionUtil.isNotEmpty(jobInfo)) {
                    //因为是模糊查询，需要再判断一次
                    Optional<XxlJobInfo> first = jobInfo.stream()
                            .filter(xxlJobInfo -> xxlJobInfo.getExecutorHandler().equals(xxlJob.value()))
                            .findFirst();
                    if (first.isPresent()) {
                        //存在就跳过
                        continue;
                    }
                }
                XxlJobInfo xxlJobInfo = createXxlJobInfo(xxlJobGroup, xxlJob, xxlRegister);
                Integer jobInfoId = this.addJobInfo(xxlJobInfo);
            }
        }
    }

    private XxlJobInfo createXxlJobInfo(XxlJobGroup xxlJobGroup, XxlJob xxlJob, XxlRegister xxlRegister){
        XxlJobInfo xxlJobInfo=new XxlJobInfo();
        xxlJobInfo.setJobGroup(xxlJobGroup.getId());
        xxlJobInfo.setJobDesc(xxlRegister.jobDesc());
        xxlJobInfo.setAuthor(xxlRegister.author());
        xxlJobInfo.setScheduleType("CRON");
        xxlJobInfo.setScheduleConf(xxlRegister.cron());
        xxlJobInfo.setGlueType("BEAN");
        xxlJobInfo.setExecutorHandler(xxlJob.value());
        xxlJobInfo.setExecutorRouteStrategy(xxlRegister.executorRouteStrategy());
        xxlJobInfo.setMisfireStrategy("DO_NOTHING");
        xxlJobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");
        xxlJobInfo.setExecutorTimeout(0);
        xxlJobInfo.setExecutorFailRetryCount(0);
        xxlJobInfo.setGlueRemark("GLUE代码初始化");
        xxlJobInfo.setTriggerStatus(xxlRegister.triggerStatus());

        return xxlJobInfo;
    }

    public void autoRegister() {
        this.addJobInfo();
    }
}
