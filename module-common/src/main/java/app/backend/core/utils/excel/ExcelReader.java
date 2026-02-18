package app.backend.core.utils.excel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import app.backend.core.utils.excel.annotation.ExcelColumn;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 엑셀 파일 읽기 유틸리티
 *
 * <p>@ExcelColumn 어노테이션이 선언된 DTO로 엑셀 데이터를 자동 매핑합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * List&lt;UserExcelDto&gt; list = ExcelReader.readToObject(multipartFile, UserExcelDto.class);
 * </pre>
 *
 * <p>지원 타입: String, Integer, Long, Double, Float, Boolean, LocalDate, LocalDateTime
 */
@Slf4j
@UtilityClass
public class ExcelReader {

    private static final int DEFAULT_SHEET_INDEX = 0;
    private static final int DEFAULT_START_ROW = 1;

    private static final ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    /** 엑셀 파일을 DTO List로 변환 (기본 시트, 1행부터) */
    public <T> List<T> readToObject(MultipartFile multipartFile, Class<T> clazz) {
        return readToObject(multipartFile, clazz, DEFAULT_START_ROW, DEFAULT_SHEET_INDEX);
    }

    /** 엑셀 파일을 DTO List로 변환 (시트 인덱스 지정) */
    public <T> List<T> readToObject(MultipartFile multipartFile, Class<T> clazz, int sheetIndex) {
        return readToObject(multipartFile, clazz, DEFAULT_START_ROW, sheetIndex);
    }

    /** 엑셀 파일을 DTO List로 변환 (시작 행, 시트 인덱스 지정) */
    public <T> List<T> readToObject(
            MultipartFile multipartFile, Class<T> clazz, int startRow, int sheetIndex) {

        try (Workbook workbook = WorkbookFactory.create(multipartFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            if (!validateHeader(sheet.getRow(0), clazz)) {
                throw new RuntimeException("엑셀 헤더가 DTO 정의와 일치하지 않습니다.");
            }

            int rowCount = sheet.getPhysicalNumberOfRows();

            return IntStream.range(startRow, rowCount)
                    .filter(rowIndex -> hasData(sheet.getRow(rowIndex)))
                    .mapToObj(rowIndex -> mapRowToObject(clazz, sheet.getRow(rowIndex)))
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 읽기 실패", e);
        }
    }

    /** Row 데이터를 객체로 매핑 */
    private <T> T mapRowToObject(Class<T> clazz, Row row) {
        if (Objects.isNull(clazz)) {
            return null;
        }

        T object = createInstance(clazz);
        int columnIndex = 0;

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ExcelColumn.class)) {
                continue;
            }

            setFieldValue(row, columnIndex, field, object);
            columnIndex++;
        }

        return object;
    }

    /** 리플렉션으로 인스턴스 생성 */
    private <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException
                | IllegalAccessException
                | NoSuchMethodException
                | InvocationTargetException e) {
            throw new RuntimeException("DTO 인스턴스 생성 실패: " + clazz.getSimpleName(), e);
        }
    }

    /** 필드에 셀 값 설정 */
    private <T> void setFieldValue(Row row, int columnIndex, Field field, T object) {
        try {
            if (columnIndex >= row.getPhysicalNumberOfCells()) {
                return;
            }

            String cellValue = getCellValueAsString(row.getCell(columnIndex));

            if (StringUtils.isNotEmpty(cellValue)) {
                Object converted = convertType(cellValue, field.getType());
                ReflectionUtils.makeAccessible(field);
                field.set(object, converted);
            }

            // Bean Validation 체크
            Optional<ConstraintViolation<T>> violation =
                    VALIDATOR.validate(object).stream()
                            .filter(v -> v.getPropertyPath().toString().equals(field.getName()))
                            .findFirst();

            if (violation.isPresent()) {
                throw new jakarta.validation.ValidationException(violation.get().getMessage());
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException("필드 값 설정 실패: " + field.getName(), e);
        }
    }

    /** 문자열을 대상 타입으로 변환 */
    @SuppressWarnings("unchecked")
    private <T> T convertType(String value, Class<T> targetType) {
        if (targetType == String.class) {
            return (T) value;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return (T) Integer.valueOf(parseNumericString(value));
        }
        if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(parseNumericString(value));
        }
        if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(value);
        }
        if (targetType == Float.class || targetType == float.class) {
            return (T) Float.valueOf(value);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return (T) Boolean.valueOf(value);
        }
        if (targetType == LocalDate.class) {
            return (T) LocalDate.parse(value);
        }
        if (targetType == LocalDateTime.class) {
            return (T) LocalDateTime.parse(value);
        }

        return (T) value;
    }

    /** 숫자 문자열 정리 ("123.0" → "123") */
    private String parseNumericString(String value) {
        return value.endsWith(".0") ? value.substring(0, value.length() - 2) : value;
    }

    /** Row에 데이터가 있는지 확인 */
    private boolean hasData(Row row) {
        if (row == null) {
            return false;
        }

        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            if (StringUtils.isNotEmpty(getCellValueAsString(row.getCell(i)))) {
                return true;
            }
        }

        return false;
    }

    /** 셀 값을 문자열로 변환 */
    private String getCellValueAsString(Cell cell) {
        if (Objects.isNull(cell)) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getRichStringCellValue().getString();
            case NUMERIC -> processNumericCell(cell);
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> String.valueOf(cell.getCellFormula());
            case ERROR -> ErrorEval.getText(cell.getErrorCellValue());
            default -> "";
        };
    }

    /** 숫자 셀 처리 (날짜 / 숫자 구분) */
    private String processNumericCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toString();
        }

        String numericValue = String.valueOf(cell.getNumericCellValue());
        return numericValue.endsWith(".0")
                ? numericValue.substring(0, numericValue.length() - 2)
                : numericValue;
    }

    /** 엑셀 헤더와 DTO @ExcelColumn 헤더 일치 여부 확인 */
    private <T> boolean validateHeader(Row row, Class<T> clazz) {
        if (row == null) {
            return false;
        }

        List<String> excelHeaders =
                IntStream.range(0, row.getPhysicalNumberOfCells())
                        .mapToObj(index -> getCellValueAsString(row.getCell(index)))
                        .toList();

        List<String> dtoHeaders = ExcelUtils.getHeaderList(clazz);

        log.debug(">>> Excel headers: {}", excelHeaders);
        log.debug(">>> DTO headers: {}", dtoHeaders);

        return excelHeaders.equals(dtoHeaders);
    }
}
