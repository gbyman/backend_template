package app.backend.core.base.dto;

import app.backend.core.base.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/** 요청 DTO의 기본 클래스 모든 요청 DTO는 이 클래스를 상속받습니다. */
@Getter
@Setter
public abstract class BaseReqDto extends BaseVo {

    @Schema(hidden = true, description = "요청 사용자 ID")
    private String reqUserId;
}
