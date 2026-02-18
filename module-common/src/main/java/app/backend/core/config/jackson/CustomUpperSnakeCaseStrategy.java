package app.backend.core.config.jackson;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Jackson UPPER_SNAKE_CASE 네이밍 전략
 *
 * <p>Java의 camelCase 필드명을 JSON의 UPPER_SNAKE_CASE로 변환합니다.
 *
 * <p><strong>사용 사례:</strong>
 *
 * <ul>
 *   <li>레거시 시스템 API 연동 (UPPER_SNAKE_CASE 응답 필요)
 *   <li>MyBatis 등 레거시 DB 연동 (컬럼명 매핑)
 *   <li>외부 시스템과의 데이터 교환 규격 준수
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // 방법 1: ObjectMapper에 전역 설정
 * ObjectMapper mapper = new ObjectMapper();
 * mapper.setPropertyNamingStrategy(CustomUpperSnakeCaseStrategy.UPPER_SNAKE_CASE);
 *
 * // 방법 2: @JsonNaming으로 특정 클래스에만 적용
 * &#64;JsonNaming(CustomUpperSnakeCaseStrategy.class)
 * public class LegacyApiResponse {
 *     private String userName;      // JSON: "USER_NAME"
 *     private Integer userAge;      // JSON: "USER_AGE"
 *     private Boolean isActive;     // JSON: "IS_ACTIVE"
 * }
 *
 * // 방법 3: Spring Boot 전역 설정 (application.yml)
 * // spring:
 * //   jackson:
 * //     property-naming-strategy: app.backend.core.config.jackson.CustomUpperSnakeCaseStrategy
 *
 * // 변환 예시:
 * // userName         → USER_NAME
 * // userAge          → USER_AGE
 * // isActive         → IS_ACTIVE
 * // createdAt        → CREATED_AT
 * // userPhoneNumber  → USER_PHONE_NUMBER
 * </pre>
 *
 * <p><strong>⚠️ 주의사항:</strong>
 *
 * <ul>
 *   <li>전역 설정 시 모든 JSON 응답에 영향을 미치므로 신중히 사용
 *   <li>일반적으로는 @JsonNaming으로 특정 DTO에만 적용 권장
 *   <li>Jackson의 기본 SNAKE_CASE는 lower_snake_case이므로 UPPER가 필요한 경우에만 사용
 * </ul>
 */
public class CustomUpperSnakeCaseStrategy extends PropertyNamingStrategies.NamingBase {

    /** 싱글톤 인스턴스 */
    public static final PropertyNamingStrategy UPPER_SNAKE_CASE =
            new CustomUpperSnakeCaseStrategy();

    /**
     * camelCase를 UPPER_SNAKE_CASE로 변환
     *
     * <p>변환 규칙:
     *
     * <ul>
     *   <li>대문자가 나올 때마다 언더스코어 삽입 (첫 문자 제외)
     *   <li>모든 문자를 대문자로 변환
     *   <li>첫 번째 언더스코어는 무시
     * </ul>
     *
     * @param input 원본 필드명 (camelCase)
     * @return 변환된 필드명 (UPPER_SNAKE_CASE)
     */
    @Override
    public String translate(String input) {
        if (input == null) {
            return null;
        }

        int length = input.length();
        StringBuilder result = new StringBuilder(length * 2);
        int resultLength = 0;
        boolean wasPrevTranslated = false;

        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);

            // 첫 번째 언더스코어는 건너뛰기
            if (i > 0 || c != '_') {
                // 대문자인 경우 언더스코어 삽입
                if (Character.isUpperCase(c)) {
                    // 이전에 변환되지 않았고, 결과가 비어있지 않고, 마지막 문자가 언더스코어가 아닌 경우
                    if (!wasPrevTranslated
                            && resultLength > 0
                            && result.charAt(resultLength - 1) != '_') {
                        result.append('_');
                        resultLength++;
                    }
                    wasPrevTranslated = true;
                } else {
                    wasPrevTranslated = false;
                }

                result.append(c);
                resultLength++;
            }
        }

        return resultLength > 0 ? result.toString().toUpperCase() : input;
    }
}
