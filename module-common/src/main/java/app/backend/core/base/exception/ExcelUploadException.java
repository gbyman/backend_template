package app.backend.core.base.exception;

import java.util.List;

import app.backend.core.utils.excel.vo.ExcelErrorVo;
import lombok.Getter;

/**
 * 엑셀 업로드 에러 예외
 *
 * <p>엑셀 업로드 시 검증 에러가 발생하면 에러 목록과 함께 throw합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * List&lt;ExcelErrorVo&gt; errors = new ArrayList&lt;&gt;();
 * errors.add(ExcelUtils.createError(2, new String[]{"이름"}, "필수 값입니다."));
 * throw new ExcelUploadException(errors);
 * </pre>
 */
@Getter
public class ExcelUploadException extends RuntimeException {

    private final List<ExcelErrorVo> errors;

    public ExcelUploadException(List<ExcelErrorVo> errors) {
        super("엑셀 업로드 검증 실패: " + errors.size() + "건의 에러");
        this.errors = List.copyOf(errors);
    }
}
