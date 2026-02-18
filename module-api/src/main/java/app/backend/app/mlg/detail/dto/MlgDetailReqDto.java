package app.backend.app.mlg.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "다국어 상세 요청")
public class MlgDetailReqDto {

    @Schema(description = "언어 구분값 (ISO 639-1)", example = "ko")
    @NotBlank(message = "언어 구분값은 필수입니다")
    @Size(max = 10, message = "언어 구분값은 10자 이내로 입력해주세요")
    private String langDivVal;

    @Schema(description = "언어 내용", example = "저장")
    @NotBlank(message = "언어 내용은 필수입니다")
    @Size(max = 500, message = "언어 내용은 500자 이내로 입력해주세요")
    private String langContent;

    @Schema(description = "비고", example = "저장 버튼 라벨")
    @Size(max = 500, message = "비고는 500자 이내로 입력해주세요")
    private String remarkContent;
}
