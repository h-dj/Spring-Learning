package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.dto.ClassGroupDTO;
import cn.reid.springbatchdemo.entity.ClassGroup;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClassGroupMapper {

    ClassGroupDTO toDto(ClassGroup group);
}
