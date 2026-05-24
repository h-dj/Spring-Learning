package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.dto.EnrollmentDTO;
import cn.reid.springbatchdemo.entity.Enrollment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    EnrollmentDTO toDto(Enrollment enrollment);
}
