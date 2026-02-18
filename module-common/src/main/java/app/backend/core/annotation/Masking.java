package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import app.backend.core.constants.MaskingType;
import app.backend.core.serializer.MaskingSerializer;

/**
 * 민감정보 마스킹 어노테이션 DTO 필드에 사용하여 JSON 직렬화 시 자동으로 마스킹 처리합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * public class UserDto {
 *     @Masking(type = MaskingType.NAME)
 *     private String name;
 *
 *     @Masking(type = MaskingType.EMAIL)
 *     private String email;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = MaskingSerializer.class)
public @interface Masking {
    MaskingType type();
}
