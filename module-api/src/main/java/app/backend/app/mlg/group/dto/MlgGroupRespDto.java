package app.backend.app.mlg.group.dto;

import java.util.List;

import app.backend.app.mlg.detail.dto.MlgDetailRespDto;
import app.backend.core.base.dto.BaseRespDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "다국어 그룹 응답")
public class MlgGroupRespDto extends BaseRespDto {

    @Schema(description = "다국어 코드값")
    private String mlgCodeVal;

    @Schema(description = "사용 여부")
    private boolean useYn;

    @Schema(description = "비고")
    private String remarkContent;

    @Schema(description = "다국어 상세 목록")
    private List<MlgDetailRespDto> details;
}
