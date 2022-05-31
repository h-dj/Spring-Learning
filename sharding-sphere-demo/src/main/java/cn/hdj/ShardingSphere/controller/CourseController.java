package cn.hdj.ShardingSphere.controller;

import cn.hdj.ShardingSphere.po.CoursePO;
import cn.hdj.ShardingSphere.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/5/31 16:26
 */
@RestController
@RequestMapping(value = "/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping(value = "/create")
    public ResponseEntity create(@RequestBody CoursePO coursePO) {
        this.courseService.save(coursePO);
        return ResponseEntity.ok().body("ok");
    }

    @GetMapping(value = "/query")
    public ResponseEntity query() {
        List<CoursePO> list = this.courseService.list();
        return ResponseEntity
                .ok(list);
    }
}
