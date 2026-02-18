package app.backend.core.utils.excel.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** 엑셀 업로드 에러 정보 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExcelErrorVo {
    /** 에러 발생 행 번호 */
    private long row;

    /** 시트 번호 */
    private Integer sheetNo;

    /** 에러 발생 헤더명 */
    private String[] headers;

    /** 에러 메시지 */
    private String message;

    /** 에러 관련 데이터 */
    private Object data;
}
