package app.backend.app.sample.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import app.backend.app.sample.dto.SampleRespDto;
import app.backend.app.sample.entity.SampleEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SampleMapStruct {
    SampleRespDto toDto(SampleEntity entity);
}
