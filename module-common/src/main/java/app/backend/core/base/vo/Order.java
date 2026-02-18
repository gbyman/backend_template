package app.backend.core.base.vo;

import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 정렬 조건 VO
 *
 * <p>프론트엔드에서 받은 정렬 조건을 Spring Data Sort로 변환하기 위한 VO
 *
 * <p>사용 예시:
 *
 * <pre>
 * List&lt;Order&gt; orders = List.of(
 *     new Order("name", Sort.Direction.ASC),
 *     new Order("createdAt", Sort.Direction.DESC)
 * );
 * Sort sort = SortUtils.getSort(orders);
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Schema(description = "정렬 컬럼명", example = "createdAt")
    private String column;

    @Schema(
            description = "정렬 방향",
            example = "DESC",
            allowableValues = {"ASC", "DESC"})
    private Sort.Direction direction;
}
