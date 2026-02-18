package app.backend.app.mlg.group.dto;

import java.util.List;

import app.backend.app.mlg.detail.dto.MlgDetailReqDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class MlgGroupReqDto {

    @Getter
    @Setter
    @Schema(description = "다국어 그룹 생성 요청")
    public static class Create {

        @Schema(description = "사용 여부", example = "true")
        private boolean useYn = true;

        @Schema(description = "비고", example = "저장 버튼")
        @Size(max = 500, message = "비고는 500자 이내로 입력해주세요")
        private String remarkContent;

        @Schema(description = "다국어 상세 목록")
        @NotEmpty(message = "다국어 상세 목록은 필수입니다")
        @Valid
        private List<MlgDetailReqDto> details;
    }

    @Getter
    @Setter
    @Schema(description = "다국어 그룹 수정 요청")
    public static class Update {

        @Schema(description = "사용 여부", example = "true")
        private boolean useYn = true;

        @Schema(description = "비고", example = "저장 버튼")
        @Size(max = 500, message = "비고는 500자 이내로 입력해주세요")
        private String remarkContent;

        @Schema(description = "다국어 상세 목록")
        @NotEmpty(message = "다국어 상세 목록은 필수입니다")
        @Valid
        private List<MlgDetailReqDto> details;
    }
}
