package cn.reid.springbatchdemo.mapper;

import cn.reid.springbatchdemo.dto.ExamScoreDTO;
import cn.reid.springbatchdemo.entity.ExamScore;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExamScoreMapper {

    ExamScoreDTO toDto(ExamScore examScore);
}
