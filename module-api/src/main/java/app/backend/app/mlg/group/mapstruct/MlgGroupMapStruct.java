package app.backend.app.mlg.group.mapstruct;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import app.backend.app.mlg.detail.mapstruct.MlgDetailMapStruct;
import app.backend.app.mlg.group.dto.MlgGroupReqDto;
import app.backend.app.mlg.group.dto.MlgGroupRespDto;
import app.backend.app.mlg.group.entity.MlgGroupEntity;
import app.backend.core.mapstruct.MapStructConfig;

@Mapper(config = MapStructConfig.class, uses = MlgDetailMapStruct.class)
public interface MlgGroupMapStruct {

    @Mapping(source = "dto.details", target = "details")
    @Mapping(target = "mlgCodeVal", source = "mlgCodeVal")
    MlgGroupEntity toEntity(MlgGroupReqDto.Create dto, String mlgCodeVal);

    MlgGroupRespDto toDto(MlgGroupEntity entity);

    @AfterMapping
    default void linkGroupToDetails(@MappingTarget MlgGroupEntity group) {
        if (group.getDetails() != null) {
            group.getDetails().forEach(detail -> detail.setMlgGroup(group));
        }
    }
}
