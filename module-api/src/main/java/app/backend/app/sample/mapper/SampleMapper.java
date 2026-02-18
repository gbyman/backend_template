package app.backend.app.sample.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import app.backend.app.sample.dto.SampleRespDto;

@Mapper
public interface SampleMapper {
    List<SampleRespDto> findAll();

    SampleRespDto findById(@Param("id") Long id);
}
