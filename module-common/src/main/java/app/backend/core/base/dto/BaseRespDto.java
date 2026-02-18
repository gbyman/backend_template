package app.backend.core.base.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 응답 DTO의 기본 클래스 모든 응답 DTO는 이 클래스를 상속받아 생성/수정 정보를 포함합니다. */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseRespDto {

    @Schema(description = "등록자 ID")
    private String regUserId;

    @Schema(description = "등록일시")
    private LocalDateTime regDatetime;

    @Schema(description = "수정자 ID")
    private String updaterId;

    @Schema(description = "수정일시")
    private LocalDateTime updateDatetime;
}
