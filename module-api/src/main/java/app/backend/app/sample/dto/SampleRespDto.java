package app.backend.app.sample.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "샘플 응답")
public class SampleRespDto {

    @Schema(description = "샘플 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "샘플 제목")
    private String title;

    @Schema(description = "내용", example = "샘플 내용입니다")
    private String content;

    @Schema(description = "사용 여부", example = "Y")
    private String useYn;

    @Schema(description = "등록자 ID", example = "admin")
    private String regUserId;

    @Schema(description = "등록 일시", example = "2025-01-01T12:00:00")
    private LocalDateTime regDatetime;

    @Schema(description = "수정자 ID", example = "admin")
    private String updaterId;

    @Schema(description = "수정 일시", example = "2025-01-01T12:00:00")
    private LocalDateTime updateDatetime;
}
