package app.backend.core.utils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.experimental.UtilityClass;

/**
 * 중복 제거 유틸리티
 *
 * <p>리스트에서 중복된 요소를 제거하는 기능을 제공합니다. Thread-safe한 방식으로 구현되어 있습니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * // 객체의 특정 필드를 기준으로 중복 제거
 * List&lt;User&gt; users = ...;
 * List&lt;User&gt; uniqueUsers = DeduplicationUtils.deduplication(users, User::getEmail);
 *
 * // 객체 자체를 기준으로 중복 제거
 * List&lt;String&gt; items = List.of("a", "b", "a", "c", "b");
 * List&lt;String&gt; uniqueItems = DeduplicationUtils.deduplication(items);
 * // 결과: ["a", "b", "c"]
 * </pre>
 */
@UtilityClass
public class DeduplicationUtils {

    /**
     * 특정 키 함수를 기준으로 리스트의 중복을 제거
     *
     * <p>Thread-safe하게 구현되어 병렬 스트림에서도 안전하게 사용할 수 있습니다.
     *
     * @param list 중복이 있는 리스트
     * @param keyExtractor 중복 여부를 판단할 키 추출 함수 (예: User::getId)
     * @param <T> 리스트 요소 타입
     * @return 중복이 제거된 리스트
     */
    public <T> List<T> deduplication(final List<T> list, Function<? super T, ?> keyExtractor) {
        return list.stream().filter(distinctByKey(keyExtractor)).toList();
    }

    /**
     * Thread-safe한 중복 제거 Predicate 생성
     *
     * <p>ConcurrentHashMap.newKeySet()을 사용하여 thread-safe를 보장합니다.
     *
     * @param keyExtractor 키 추출 함수
     * @param <T> 요소 타입
     * @return 중복 제거를 위한 Predicate
     */
    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = ConcurrentHashMap.newKeySet();
        return element -> seen.add(keyExtractor.apply(element));
    }

    /**
     * 리스트에서 중복된 요소 제거
     *
     * <p>객체의 equals/hashCode를 기준으로 중복을 판단합니다.
     *
     * @param list 중복이 있는 리스트
     * @param <T> 리스트 요소 타입
     * @return 중복이 제거된 리스트
     */
    public <T> List<T> deduplication(final List<T> list) {
        return list.stream().distinct().toList();
    }
}
