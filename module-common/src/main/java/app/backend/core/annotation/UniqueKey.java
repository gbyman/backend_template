package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 중복 제거 시 고유 키로 사용할 필드를 표시하는 어노테이션
 *
 * <p>Excel 업로드 등에서 특정 필드 조합을 기준으로 중복을 제거할 때 사용합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * public class UserDto {
 *     @UniqueKey
 *     private String userId;
 *
 *     @UniqueKey
 *     private String email;
 *
 *     private String name;
 * }
 *
 * // userId와 email 조합으로 중복 제거
 * List&lt;UserDto&gt; uniqueList = DistinctListUtils.removeDuplicatesByUniqueKeys(userList);
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueKey {}
