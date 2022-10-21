package cn.hdj.fastboot.modules.test.controller;

import cn.hdj.fastboot.modules.test.domain.UserA;
import cn.hdj.fastboot.modules.test.domain.UserB;
import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/4/15 15:45
 */
@Tag(name = "缓存操作接口", description = "缓存操作描述")
@RestController
@RequestMapping(value = "/cache")
@Slf4j
public class TestCacheController {

    @Operation(summary = "列表数据", description = "列表数据描述")
    @Cacheable(cacheNames = "test",key = "#num")
    @GetMapping("/list")
    public UserA list(@RequestParam Integer num) {
        log.info("生成数据================>");
        List<UserA> list = Stream.generate(RandomUtil::randomInt)
                .map(v->{
                    UserA userA = new UserA();
                    userA.setNum(v);
                    return userA;
                })
                .limit(num)
                .collect(Collectors.toList());
        return list.get(0);
    }

    @Operation(summary = "列表数据", description = "列表数据描述")
    @Cacheable(cacheNames = "test",key = "#num")
    @GetMapping("/list2")
    public UserB list2(@RequestParam Integer num) {
        log.info("生成数据================>");
        List<UserB> list = Stream.generate(RandomUtil::randomInt)
                .map(v->{
                    UserB userA = new UserB();
                    userA.setNum(v);
                    return userA;
                })
                .limit(num)
                .collect(Collectors.toList());
        return list.get(0);
    }

}
