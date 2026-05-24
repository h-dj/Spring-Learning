package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.dto.CourseDTO;
import cn.reid.springbatchdemo.entity.Course;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseDTO toDto(Course course);
}
