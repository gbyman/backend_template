package app.backend.core.utils.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 엑셀 파일명 지정 어노테이션
 *
 * <p>다운로드 시 기본 파일명을 설정합니다.
 *
 * <pre>
 * &#64;ExcelFile(filename = "사용자목록")
 * &#64;ExcelSheet(sheetName = "Sheet1")
 * public class UserExcelDto implements ExcelRowConvertor { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelFile {
    /** 다운로드 파일명 (미지정 시 클래스명 사용) */
    String filename() default "";
}
