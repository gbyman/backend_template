package app.backend.core.utils.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 엑셀 시트 설정 어노테이션
 *
 * <p>시트명과 스타일 적용 여부를 설정합니다.
 *
 * <pre>
 * &#64;ExcelSheet(sheetName = "사용자", style = true)
 * public class UserExcelDto implements ExcelRowConvertor { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSheet {
    /** 시트명 (미지정 시 기본 시트명) */
    String sheetName() default "";

    /** 헤더 스타일 적용 여부 (노란 배경, 굵은 글씨, 테두리) */
    boolean style() default true;
}
