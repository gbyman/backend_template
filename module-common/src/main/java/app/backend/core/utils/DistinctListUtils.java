package app.backend.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.backend.core.annotation.UniqueKey;
import lombok.experimental.UtilityClass;

/**
 * 리스트 중복 제거 및 분할 유틸리티
 *
 * <p>@UniqueKey 어노테이션을 사용한 중복 제거와 리스트 분할 기능을 제공합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * // @UniqueKey 필드 기준 중복 제거
 * public class UserDto {
 *     @UniqueKey
 *     private String userId;
 *     @UniqueKey
 *     private String email;
 *     private String name;
 * }
 * List&lt;UserDto&gt; uniqueUsers = DistinctListUtils.removeDuplicatesByUniqueKeys(userList);
 *
 * // 리스트 분할 (배치 처리 시 유용)
 * List&lt;List&lt;User&gt;&gt; chunks = DistinctListUtils.splitList(users, 100);
 * chunks.forEach(chunk -> processBatch(chunk));
 * </pre>
 */
@UtilityClass
public class DistinctListUtils {

    /**
     * @UniqueKey 어노테이션이 붙은 필드들을 기준으로 중복 제거
     *
     * <p>여러 필드에 @UniqueKey가 붙어 있으면 해당 필드들의 조합으로 중복을 판단합니다.
     *
     * @param dtoList 중복 제거할 리스트
     * @param <T> 리스트 요소 타입
     * @return 중복이 제거된 리스트
     * @throws RuntimeException 리플렉션 접근 오류 시
     */
    public <T> List<T> removeDuplicatesByUniqueKeys(List<T> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> uniqueKeySet = new HashSet<>();
        List<T> resultList = new ArrayList<>();

        for (T dto : dtoList) {
            String uniqueKey = generateUniqueKey(dto);
            if (uniqueKeySet.add(uniqueKey)) {
                resultList.add(dto);
            }
        }

        return resultList;
    }

    /**
     * @UniqueKey 어노테이션이 붙은 필드들의 값을 조합하여 고유 키 생성
     *
     * @param dto 키를 생성할 객체
     * @param <T> 객체 타입
     * @return 고유 키 문자열 (필드값1:필드값2:... 형식)
     * @throws RuntimeException 리플렉션 접근 오류 시
     */
    private <T> String generateUniqueKey(T dto) {
        StringBuilder keyBuilder = new StringBuilder();
        Class<?> dtoClass = dto.getClass();

        try {
            for (Field field : dtoClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(UniqueKey.class)) {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    keyBuilder.append(value).append(":");
                }
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException("@UniqueKey 필드 접근 중 오류 발생: " + dtoClass.getName(), e);
        }

        return keyBuilder.toString();
    }

    /**
     * 리스트에서 중복 제거 (equals/hashCode 기반)
     *
     * @param list 중복 제거할 리스트
     * @param <T> 리스트 요소 타입
     * @return 중복이 제거된 리스트
     */
    public <T> List<T> removeDuplicates(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().distinct().toList();
    }

    /**
     * 리스트를 지정된 크기로 분할
     *
     * <p>배치 처리나 대량 데이터 처리 시 유용합니다.
     *
     * @param list 분할할 리스트
     * @param chunkSize 각 청크의 크기
     * @param <T> 리스트 요소 타입
     * @return 분할된 리스트들의 리스트
     */
    public <T> List<List<T>> splitList(List<T> list, int chunkSize) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize는 0보다 커야 합니다. (입력값: " + chunkSize + ")");
        }

        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            int end = Math.min(list.size(), i + chunkSize);
            chunks.add(new ArrayList<>(list.subList(i, end)));
        }
        return chunks;
    }
}
