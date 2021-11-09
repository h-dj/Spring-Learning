package cn.hdj.scene.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description: 使用 HyperLogLog 统计服务
 * @Author cloud-inman
 * @Date 2021/11/5 14:33
 */
@Service
@Slf4j
public class HyperLogLogStatisticService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 初始化数据
     */
    public void init() {
        //查询 数据库

        List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from \"access_log\" where \"user_id\" is not NULL ");

        final HyperLogLogOperations<String, String> opsForHyperLogLog = stringRedisTemplate.opsForHyperLogLog();

        maps.stream()
                .forEach(v -> {
                    String entity_type = MapUtil.getStr(v, "entity_type");
                    String url = MapUtil.getStr(v, "url");
                    String user_id = MapUtil.getStr(v, "user_id");
                    String create_time = DateUtil.formatDate(MapUtil.getDate(v, "create_time"));

                    String dau_hll_key = "%s:dau:%s";

                    log.info("处理 " + user_id + " " + create_time);

                    if (StrUtil.equals(entity_type, "exponential") || StrUtil.indexOfIgnoreCase(url, "hzeyun.com") > 0) {
                        //hzeyun
                        opsForHyperLogLog
                                .add(String.format(dau_hll_key, "hzeyun", create_time), user_id);

                    } else if (StrUtil.indexOfIgnoreCase(url, "ibuychem.com") > 0) {
                        //ibuychem

                        opsForHyperLogLog.add(String.format(dau_hll_key, "ibuychem", create_time), user_id);
                    }
                });
    }
}
