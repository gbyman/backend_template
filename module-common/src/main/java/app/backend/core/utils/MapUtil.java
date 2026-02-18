package app.backend.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Map과 VO 객체 간 변환 유틸리티
 *
 * <p>주로 MyBatis 등 레거시 시스템 연동 시 사용됩니다. JPA 사용 시에는 필요성이 낮습니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * // Map → VO 변환 (UPPER_SNAKE_CASE → camelCase)
 * Map&lt;String, Object&gt; map = Map.of("USER_NAME", "홍길동", "USER_AGE", 30);
 * UserDto user = MapUtil.convertToValueObject(map, UserDto.class);
 *
 * // VO → Map 변환
 * Map&lt;String, Object&gt; resultMap = MapUtil.convertToMap(user);
 * </pre>
 */
@Slf4j
@UtilityClass
public class MapUtil {

    /**
     * Map을 Object로 변환 (Setter 메서드 기반)
     *
     * @param map 변환할 Map
     * @param obj 대상 객체 (빈 인스턴스)
     * @return 변환된 객체
     */
    public Object convertMapToObject(Map<String, Object> map, Object obj) {
        if (map == null || obj == null) {
            return obj;
        }

        String setMethodPrefix = "set";

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String methodName =
                    setMethodPrefix + key.substring(0, 1).toUpperCase() + key.substring(1);
            Method[] methods = obj.getClass().getDeclaredMethods();

            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    try {
                        method.invoke(obj, entry.getValue());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.warn(
                                "Failed to invoke setter method: {} for key: {}",
                                methodName,
                                key,
                                e);
                    }
                }
            }
        }

        return obj;
    }

    /**
     * Object를 Map으로 변환
     *
     * @param obj 변환할 객체
     * @return 변환된 Map (필드명 → 필드값)
     */
    public Map<String, Object> convertToMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> convertMap = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                convertMap.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                log.warn("Failed to access field: {}", field.getName(), e);
            }
        }

        return convertMap;
    }

    /**
     * Map을 VO 객체로 변환 (UPPER_SNAKE_CASE → camelCase 자동 변환)
     *
     * <p>MyBatis 결과 매핑 시 주로 사용됩니다.
     *
     * @param map 변환할 Map (UPPER_SNAKE_CASE 키)
     * @param type 대상 클래스 타입
     * @param <T> 반환 타입
     * @return 변환된 VO 객체
     * @throws RuntimeException 변환 실패 시
     */
    public <T> T convertToValueObject(Map<String, Object> map, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Class type cannot be null");
        }

        try {
            T instance = type.getDeclaredConstructor().newInstance();

            if (map == null || map.isEmpty()) {
                return instance;
            }

            Field[] fields = type.getDeclaredFields();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }

                String mapKey = toCamelCase(entry.getKey());

                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();

                    if (mapKey.equals(fieldName)) {
                        setFieldValue(instance, field, entry.getValue());
                        break;
                    }
                }
            }

            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Failed to convert Map to ValueObject: " + type.getName(), e);
        }
    }

    /**
     * UPPER_SNAKE_CASE를 camelCase로 변환
     *
     * @param snakeCase UPPER_SNAKE_CASE 문자열 (예: USER_NAME)
     * @return camelCase 문자열 (예: userName)
     */
    private String toCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }

        StringBuilder result = new StringBuilder();
        String[] parts = snakeCase.toLowerCase().split("_");

        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                result.append(parts[i]);
            } else {
                result.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
            }
        }

        return result.toString();
    }

    /**
     * 필드에 값을 설정 (타입 변환 처리)
     *
     * @param instance 대상 객체
     * @param field 필드
     * @param value 설정할 값
     */
    private void setFieldValue(Object instance, Field field, Object value)
            throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        Class<?> valueType = value.getClass();

        // 타입이 일치하면 바로 설정
        if (valueType.equals(fieldType)) {
            field.set(instance, value);
            return;
        }

        // Integer → int 변환 처리
        if (value instanceof Integer && fieldType.equals(int.class)) {
            field.set(instance, value);
            return;
        }

        // Long → long 변환 처리
        if (value instanceof Long && fieldType.equals(long.class)) {
            field.set(instance, value);
            return;
        }

        // Double → double 변환 처리
        if (value instanceof Double && fieldType.equals(double.class)) {
            field.set(instance, value);
            return;
        }

        // Boolean → boolean 변환 처리
        if (value instanceof Boolean && fieldType.equals(boolean.class)) {
            field.set(instance, value);
            return;
        }

        log.warn(
                "Type mismatch for field {}: expected {}, got {}",
                field.getName(),
                fieldType.getName(),
                valueType.getName());
    }

    /**
     * List&lt;Object&gt;를 List&lt;Map&gt;으로 변환
     *
     * @param list 변환할 리스트
     * @return 변환된 리스트
     */
    public List<Map<String, Object>> convertToMaps(List<?> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> convertList = new ArrayList<>();

        for (Object obj : list) {
            convertList.add(convertToMap(obj));
        }

        return convertList;
    }

    /**
     * List&lt;Map&gt;을 List&lt;VO&gt;로 변환
     *
     * @param list 변환할 Map 리스트
     * @param type 대상 클래스 타입
     * @param <T> 반환 타입
     * @return 변환된 VO 리스트
     */
    public <T> List<T> convertToValueObjects(List<Map<String, Object>> list, Class<T> type) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> convertList = new ArrayList<>();

        for (Map<String, Object> map : list) {
            convertList.add(convertToValueObject(map, type));
        }

        return convertList;
    }

    /**
     * 두 Map을 병합
     *
     * <p>두 번째 Map의 값이 첫 번째 Map의 값을 덮어씁니다.
     *
     * @param map1 첫 번째 Map
     * @param map2 두 번째 Map
     * @return 병합된 Map
     */
    public Map<String, Object> mergeMaps(Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> resultMap = new HashMap<>();

        if (map1 != null) {
            resultMap.putAll(map1);
        }

        if (map2 != null) {
            resultMap.putAll(map2);
        }

        return resultMap;
    }
}
