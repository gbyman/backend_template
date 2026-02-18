package app.backend.core.utils;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Sort;

import app.backend.core.base.vo.Order;
import lombok.experimental.UtilityClass;

/**
 * 정렬 유틸리티
 *
 * <p>프론트엔드에서 받은 정렬 조건(Order VO)을 Spring Data Sort로 변환합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * // Controller에서
 * public Page&lt;UserDto&gt; getUsers(@RequestBody List&lt;Order&gt; orders, Pageable pageable) {
 *     Sort sort = SortUtils.getSort(orders);
 *     Pageable pageableWithSort = PageRequest.of(
 *         pageable.getPageNumber(),
 *         pageable.getPageSize(),
 *         sort
 *     );
 *     return userService.findAll(pageableWithSort);
 * }
 * </pre>
 */
@UtilityClass
public class SortUtils {

    /** 기본 정렬 컬럼 */
    private static final String DEF_SORT_COLUMN = "createdAt";

    /** 기본 정렬 방향 */
    private static final Sort.Direction DEF_DIRECTION = Sort.Direction.DESC;

    /**
     * Order VO 리스트를 Spring Data Sort로 변환
     *
     * <p>리스트가 null이거나 비어있으면 기본 정렬(createdAt DESC)을 반환합니다.
     *
     * @param orders 정렬 조건 리스트
     * @return Spring Data Sort 객체
     */
    public Sort getSort(List<Order> orders) {

        if (Objects.isNull(orders) || orders.isEmpty()) {
            return Sort.by(DEF_DIRECTION, DEF_SORT_COLUMN);
        }

        List<Sort.Order> sortOrders =
                orders.stream()
                        .filter(Objects::nonNull)
                        .map(order -> new Sort.Order(order.getDirection(), order.getColumn()))
                        .toList();

        if (sortOrders.isEmpty()) {
            return Sort.by(DEF_DIRECTION, DEF_SORT_COLUMN);
        }

        return Sort.by(sortOrders);
    }

    /**
     * Order VO 리스트를 Spring Data Sort로 변환 (기본값 지정 가능)
     *
     * @param orders 정렬 조건 리스트
     * @param defaultColumn 기본 정렬 컬럼
     * @param defaultDirection 기본 정렬 방향
     * @return Spring Data Sort 객체
     */
    public Sort getSort(List<Order> orders, String defaultColumn, Sort.Direction defaultDirection) {

        if (Objects.isNull(orders) || orders.isEmpty()) {
            return Sort.by(defaultDirection, defaultColumn);
        }

        List<Sort.Order> sortOrders =
                orders.stream()
                        .filter(Objects::nonNull)
                        .map(order -> new Sort.Order(order.getDirection(), order.getColumn()))
                        .toList();

        if (sortOrders.isEmpty()) {
            return Sort.by(defaultDirection, defaultColumn);
        }

        return Sort.by(sortOrders);
    }
}
