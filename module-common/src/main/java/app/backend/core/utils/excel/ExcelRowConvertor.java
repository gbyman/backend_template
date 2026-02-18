package app.backend.core.utils.excel;

import java.util.List;

/**
 * 엑셀 행 변환 인터페이스
 *
 * <p>엑셀 다운로드 시 DTO의 필드 값을 행 데이터로 변환합니다.
 *
 * <pre>
 * public class UserExcelDto implements ExcelRowConvertor {
 *     &#64;ExcelColumn(headerName = "이름")
 *     private String name;
 *
 *     &#64;ExcelColumn(headerName = "나이")
 *     private Integer age;
 *
 *     &#64;Override
 *     public List&lt;Object&gt; convertToExcelFormat() {
 *         return List.of(name, age);
 *     }
 * }
 * </pre>
 */
public interface ExcelRowConvertor {
    List<Object> convertToExcelFormat();
}
