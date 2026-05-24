package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.dto.TeacherDTO;
import cn.reid.springbatchdemo.entity.Teacher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    TeacherDTO toDto(Teacher teacher);
}
