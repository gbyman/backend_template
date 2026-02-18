package app.backend.core.utils.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 엑셀 컬럼 매핑 어노테이션
 *
 * <p>DTO 필드에 선언하면 엑셀 헤더와 매핑됩니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * public class UserExcelDto implements ExcelRowConvertor {
 *     &#64;ExcelColumn(headerName = "이름")
 *     private String name;
 *
 *     &#64;ExcelColumn(headerName = "이메일")
 *     private String email;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {
    /** 엑셀 헤더명 (미지정 시 필드명 사용) */
    String headerName() default "";
}
