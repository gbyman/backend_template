package app.backend.core.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Null 필드 기본값 초기화 유틸리티
 *
 * <p><strong>⚠️ 이 클래스는 레거시 코드 호환용입니다.</strong> 새 프로젝트에서는 사용하지 마세요.
 *
 * <p><strong>권장하는 대안 방법:</strong>
 *
 * <pre>
 * // ❌ 비권장: NullInitializer 사용
 * UserDto user = new UserDto();
 * NullInitializer.initializeNullFields(user);
 *
 * // ✅ 권장 1: 필드 선언 시 기본값 설정
 * public class UserDto {
 *     private String name = "";
 *     private Integer age = 0;
 *     private Boolean active = false;
 * }
 *
 * // ✅ 권장 2: Lombok @Builder.Default 사용
 * &#64;Builder
 * public class UserDto {
 *     &#64;Builder.Default
 *     private String name = "";
 *
 *     &#64;Builder.Default
 *     private List&lt;String&gt; tags = new ArrayList&lt;&gt;();
 * }
 *
 * // ✅ 권장 3: @NonNull 어노테이션으로 null 방지
 * public class UserDto {
 *     &#64;NonNull
 *     private String name;  // null 할당 시 NullPointerException 발생
 * }
 *
 * // ✅ 권장 4: Optional 사용
 * public class UserDto {
 *     private Optional&lt;String&gt; email = Optional.empty();
 * }
 * </pre>
 *
 * <p><strong>NullInitializer를 사용하지 말아야 하는 이유:</strong>
 *
 * <ul>
 *   <li><strong>성능 문제:</strong> 리플렉션 사용으로 성능 오버헤드 발생
 *   <li><strong>타입 안전성 부족:</strong> 컴파일 시점에 null 체크 불가
 *   <li><strong>예측 불가능:</strong> 어떤 필드가 초기화되는지 코드만 보고 알기 어려움
 *   <li><strong>유지보수 어려움:</strong> 암시적 동작으로 디버깅 어려움
 * </ul>
 *
 * <p>기존 사용 예시 (레거시 호환용):
 *
 * <pre>
 * // null 필드를 기본값으로 초기화
 * UserDto user = new UserDto();
 * NullInitializer.initializeNullFields(user);
 * // user.name = ""
 * // user.age = 0
 * // user.active = false
 * </pre>
 *
 * @deprecated 레거시 코드 호환용. 새 코드에서는 위의 권장 방법을 사용하세요.
 */
@Slf4j
@UtilityClass
@Deprecated(since = "1.0", forRemoval = true)
public class NullInitializer {

    /**
     * 객체의 null 필드를 타입별 기본값으로 초기화
     *
     * <p>초기화 규칙:
     *
     * <ul>
     *   <li>String → ""
     *   <li>Integer → 0
     *   <li>Long → 0L
     *   <li>Double → 0.0
     *   <li>Float → 0.0f
     *   <li>Boolean → false
     *   <li>BigDecimal → BigDecimal.ZERO
     *   <li>List → new ArrayList()
     *   <li>Set → new HashSet()
     *   <li>Map → new HashMap()
     * </ul>
     *
     * @param obj 초기화할 객체
     */
    public void initializeNullFields(Object obj) {
        if (obj == null) {
            log.warn("Cannot initialize null fields on a null object");
            return;
        }

        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            try {
                // 필드 값이 null인 경우에만 초기화
                if (field.get(obj) == null) {
                    Object defaultValue = getDefaultValue(field.getType());
                    if (defaultValue != null) {
                        field.set(obj, defaultValue);
                        log.debug(
                                "Initialized null field '{}' in class '{}' with default value: {}",
                                field.getName(),
                                clazz.getSimpleName(),
                                defaultValue);
                    }
                }
            } catch (IllegalAccessException e) {
                log.error(
                        "Failed to initialize field '{}' in class '{}'",
                        field.getName(),
                        clazz.getSimpleName(),
                        e);
            }
        }
    }

    /**
     * 타입에 따른 기본값 반환
     *
     * @param type 필드 타입
     * @return 기본값 (지원하지 않는 타입은 null 반환)
     */
    private Object getDefaultValue(Class<?> type) {
        if (type.equals(String.class)) {
            return "";
        } else if (type.equals(Integer.class)) {
            return 0;
        } else if (type.equals(Long.class)) {
            return 0L;
        } else if (type.equals(Double.class)) {
            return 0.0;
        } else if (type.equals(Float.class)) {
            return 0.0f;
        } else if (type.equals(Boolean.class)) {
            return false;
        } else if (type.equals(BigDecimal.class)) {
            return BigDecimal.ZERO;
        } else if (type.equals(java.util.List.class)) {
            return new ArrayList<>();
        } else if (type.equals(java.util.Set.class)) {
            return new HashSet<>();
        } else if (type.equals(java.util.Map.class)) {
            return new HashMap<>();
        }

        // 지원하지 않는 타입은 null 유지
        return null;
    }

    /**
     * 리스트의 모든 객체에 대해 null 필드 초기화
     *
     * @param list 초기화할 객체 리스트
     * @param <T> 객체 타입
     */
    public <T> void initializeNullFieldsInList(java.util.List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (T obj : list) {
            initializeNullFields(obj);
        }
    }
}
