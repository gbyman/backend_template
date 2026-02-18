package app.backend.app.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class SampleReqDto {

    @Getter
    @Setter
    @Schema(description = "샘플 생성 요청")
    public static class Create {
        @Schema(description = "제목", example = "샘플 제목")
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
        private String title;

        @Schema(description = "내용", example = "샘플 내용입니다")
        @NotBlank(message = "내용은 필수입니다")
        private String content;
    }

    @Getter
    @Setter
    @Schema(description = "샘플 수정 요청")
    public static class Update {
        @Schema(description = "제목", example = "수정된 제목")
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
        private String title;

        @Schema(description = "내용", example = "수정된 내용입니다")
        @NotBlank(message = "내용은 필수입니다")
        private String content;
    }
}
