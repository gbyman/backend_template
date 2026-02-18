package app.backend.app.mlg.detail.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import app.backend.app.mlg.detail.dto.MlgDetailReqDto;
import app.backend.app.mlg.detail.dto.MlgDetailRespDto;
import app.backend.app.mlg.detail.entity.MlgDetailEntity;
import app.backend.core.mapstruct.MapStructConfig;

@Mapper(config = MapStructConfig.class)
public interface MlgDetailMapStruct {

    @Mapping(source = "mlgGroup.mlgCodeVal", target = "mlgCodeVal")
    MlgDetailRespDto toDto(MlgDetailEntity entity);

    @Mapping(target = "mlgDetailId", ignore = true)
    @Mapping(target = "mlgGroup", ignore = true)
    MlgDetailEntity toEntity(MlgDetailReqDto dto);

    @Mapping(target = "mlgDetailId", ignore = true)
    @Mapping(target = "mlgGroup", ignore = true)
    @Mapping(target = "langDivVal", ignore = true)
    void updateEntity(MlgDetailReqDto dto, @MappingTarget MlgDetailEntity entity);
}
