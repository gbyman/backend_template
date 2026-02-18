package app.backend.core.base.vo;

import app.backend.core.base.dto.BaseReqDto;
import lombok.Getter;
import lombok.Setter;

/** 페이징 가능한 DTO의 기본 클래스 페이징이 필요한 요청 DTO는 이 클래스를 상속받습니다. */
@Getter
@Setter
public abstract class BizPageableDto extends BaseReqDto {

    private boolean pagingYn;
}
