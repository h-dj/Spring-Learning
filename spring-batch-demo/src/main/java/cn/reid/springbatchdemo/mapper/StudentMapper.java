package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.dto.StudentDTO;
import cn.reid.springbatchdemo.entity.Student;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDTO toDto(Student student);
}
