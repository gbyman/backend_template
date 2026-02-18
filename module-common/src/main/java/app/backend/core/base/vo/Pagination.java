package app.backend.core.base.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 페이징 정보를 담는 VO */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pagination extends BaseVo {

    @Positive
    @Schema(description = "페이지 사이즈", example = "10")
    @JsonProperty(value = "size")
    private Integer perPage;

    @Positive
    @Schema(description = "페이지 인덱스", example = "1")
    private Integer page;

    @Schema(description = "paging 여부", example = "true")
    private boolean pagingYn;
}
