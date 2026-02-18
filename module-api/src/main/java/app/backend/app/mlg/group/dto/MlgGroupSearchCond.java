package app.backend.app.mlg.group.dto;

import app.backend.core.base.vo.BizPageableDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "다국어 그룹 검색 조건")
public class MlgGroupSearchCond extends BizPageableDto {

    @Schema(description = "다국어 코드값", example = "MLG0000001")
    private String mlgCodeVal;

    @Schema(description = "언어 구분값", example = "ko")
    private String langDivVal;

    @Schema(description = "언어 내용 (LIKE 검색)", example = "저장")
    private String langContent;

    @Schema(description = "사용 여부", example = "true")
    private Boolean useYn;
}
