package app.backend.core.base.component;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongSupplier;

import org.springframework.data.domain.Page;
import org.springframework.data.support.PageableExecutionUtils;

import app.backend.core.base.vo.GenericListVo;

/** 서비스 공통 기능을 제공하는 추상 클래스 모든 서비스는 이 클래스를 상속받아 공통 메서드를 사용할 수 있습니다. */
public abstract class AbstractService {

    /**
     * 리스트를 GenericListVo로 변환
     *
     * @param list 변환할 리스트
     * @return GenericListVo
     */
    protected <T> GenericListVo<T> makeGenericList(List<T> list) {
        return new GenericListVo<>(list);
    }

    /**
     * 엔티티 리스트를 페이지 형태로 감싸고, DTO로 변환하여 Page 객체를 생성합니다.
     *
     * <p>이 메서드는 MyBatis 등에서 페이징 처리할 때 공통적으로 사용할 수 있으며, DTO 변환 함수(Function<E, D>)를 받아 각 엔티티를 원하는 응답
     * 타입으로 변환합니다.
     *
     * <p>count 쿼리는 지연 계산을 위해 {@link LongSupplier}를 사용하며, {@link PageableExecutionUtils}를 활용하여 실제로
     * 필요할 때만 실행됩니다.
     *
     * @param <E> 엔티티(Entity)의 타입
     * @param <D> 변환할 DTO의 타입
     * @param entityList 조회된 엔티티 리스트 (페이징된 데이터)
     * @param totalSupplier 전체 데이터 수를 제공하는 공급자 (지연 실행용)
     * @param convertFunc 엔티티를 DTO로 변환하는 함수 (예: mapStruct::toDto)
     * @return 변환된 DTO 리스트를 담은 {@link Page} 객체
     * @see PageableExecutionUtils
     * @see LongSupplier
     * @see Function
     */
    protected <E, D> Page<D> makePage(
            List<E> entityList, LongSupplier totalSupplier, Function<E, D> convertFunc) {
        Page<E> entityPage =
                PageableExecutionUtils.getPage(
                        entityList,
                        org.springframework.data.domain.PageRequest.of(0, entityList.size()),
                        totalSupplier);
        return entityPage.map(convertFunc);
    }
}
