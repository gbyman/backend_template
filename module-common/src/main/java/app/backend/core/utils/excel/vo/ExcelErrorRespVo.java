package app.backend.core.utils.excel.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 엑셀 업로드 에러 응답 VO */
@Getter
@AllArgsConstructor
public class ExcelErrorRespVo {
    private List<ExcelErrorVo> list;
}
