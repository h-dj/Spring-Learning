package cn.hdj.provider.service;

import cn.hdj.provider.entity.Student;

/**
 * @author hdj
 * @version 1.0
 * @date 2020/8/7 上午2:31
 * @description: 学生服务接口
 */
public interface StudentService {

    /**
     *   获取信息
     * @return
     */
    public Student getInfo();

    public boolean printInfo(Student student);
}
