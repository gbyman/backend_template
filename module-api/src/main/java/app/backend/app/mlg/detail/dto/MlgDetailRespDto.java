package app.backend.app.mlg.detail.dto;

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
@Schema(description = "다국어 상세 응답")
public class MlgDetailRespDto extends BaseRespDto {

    @Schema(description = "다국어 상세 ID")
    private Long mlgDetailId;

    @Schema(description = "다국어 코드값")
    private String mlgCodeVal;

    @Schema(description = "언어 구분값")
    private String langDivVal;

    @Schema(description = "언어 내용")
    private String langContent;

    @Schema(description = "비고")
    private String remarkContent;
}
