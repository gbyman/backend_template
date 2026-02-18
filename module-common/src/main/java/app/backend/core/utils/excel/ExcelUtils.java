package app.backend.core.utils.excel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import app.backend.core.utils.excel.annotation.ExcelColumn;
import app.backend.core.utils.excel.vo.ExcelErrorVo;
import lombok.experimental.UtilityClass;

/** 엑셀 공통 유틸리티 */
@UtilityClass
public class ExcelUtils {

    /**
     * 클래스의 @ExcelColumn 어노테이션에서 헤더명 목록 추출
     *
     * @param clazz 대상 클래스
     * @return 헤더명 리스트
     */
    public List<String> getHeaderList(Class<?> clazz) {
        List<String> headerList = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                String headerName = field.getAnnotation(ExcelColumn.class).headerName();
                headerList.add(StringUtils.hasText(headerName) ? headerName : field.getName());
            }
        }

        return headerList;
    }

    /**
     * 엑셀 에러 VO 생성 헬퍼
     *
     * @param rowNo 행 번호
     * @param headers 에러 발생 헤더명
     * @param message 에러 메시지
     * @param data 관련 데이터
     * @return ExcelErrorVo
     */
    public ExcelErrorVo createError(int rowNo, String[] headers, String message, Object... data) {
        return ExcelErrorVo.builder()
                .row(rowNo)
                .headers(headers)
                .message(message)
                .data(joinWithComma(data))
                .build();
    }

    /**
     * 엑셀 에러 VO 생성 헬퍼 (시트 번호 포함)
     *
     * @param rowNo 행 번호
     * @param sheetNo 시트 번호
     * @param headers 에러 발생 헤더명
     * @param message 에러 메시지
     * @param data 관련 데이터
     * @return ExcelErrorVo
     */
    public ExcelErrorVo createError(
            int rowNo, Integer sheetNo, String[] headers, String message, Object... data) {
        return ExcelErrorVo.builder()
                .row(rowNo)
                .sheetNo(sheetNo)
                .headers(headers)
                .message(message)
                .data(joinWithComma(data))
                .build();
    }

    private String joinWithComma(Object... data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return Arrays.stream(data).map(Object::toString).collect(Collectors.joining(", "));
    }
}
