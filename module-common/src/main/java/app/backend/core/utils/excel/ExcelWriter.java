package app.backend.core.utils.excel;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import app.backend.core.utils.excel.annotation.ExcelFile;
import app.backend.core.utils.excel.annotation.ExcelSheet;
import app.backend.core.utils.excel.dto.ExcelWriterDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

/**
 * 엑셀 파일 생성 및 다운로드 유틸리티
 *
 * <p>사용 예시 (컨트롤러에서):
 *
 * <pre>
 * &#64;GetMapping("/download")
 * public void download() throws IOException {
 *     List&lt;UserExcelDto&gt; data = userService.getUserList();
 *     ExcelWriter writer = new ExcelWriter(data, UserExcelDto.class);
 *     writer.write();
 * }
 * </pre>
 *
 * <p>멀티 시트:
 *
 * <pre>
 * ExcelWriter writer = new ExcelWriter(sheet1Data, Sheet1Dto.class);
 * writer.addSheet(sheet2Data, Sheet2Dto.class);
 * writer.write();
 * </pre>
 */
public class ExcelWriter {

    private final ExcelWriterDto excelWriterDto;

    @Getter private final Workbook workbook;

    public ExcelWriter(
            List<? extends ExcelRowConvertor> data, Class<? extends ExcelRowConvertor> clazz) {
        this(data, clazz, XSSFWorkbook.class);
    }

    public ExcelWriter(ExcelWriterDto excelWriterDto) {
        this(excelWriterDto, XSSFWorkbook.class);
    }

    public ExcelWriter(
            List<? extends ExcelRowConvertor> data,
            Class<? extends ExcelRowConvertor> clazz,
            Class<? extends Workbook> workbookClass) {
        this.excelWriterDto = createExcelDto(data, clazz);
        this.workbook = createWorkbook(excelWriterDto, workbookClass);
    }

    public ExcelWriter(ExcelWriterDto excelWriterDto, Class<? extends Workbook> workbookClass) {
        this.excelWriterDto = excelWriterDto;
        this.workbook = createWorkbook(this.excelWriterDto, workbookClass);
    }

    /** 시트 추가 */
    public void addSheet(
            List<? extends ExcelRowConvertor> data, Class<? extends ExcelRowConvertor> clazz) {
        setupSheet(workbook, createExcelDto(data, clazz));
    }

    /** HTTP 응답으로 엑셀 다운로드 */
    public void write() throws IOException {
        HttpServletResponse response = getServletResponse();
        if (response == null) {
            throw new RuntimeException("HttpServletResponse를 찾을 수 없습니다.");
        }

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\""
                        + getFileExtension(excelWriterDto.getFileName(), workbook)
                        + "\"");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private HttpServletResponse getServletResponse() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(ra -> ((ServletRequestAttributes) ra).getResponse())
                .orElse(null);
    }

    private Workbook createWorkbook(ExcelWriterDto excelDto, Class<? extends Workbook> cls) {
        Workbook wb = createWorkbookInstance(cls);
        setupSheet(wb, excelDto);
        return wb;
    }

    private ExcelWriterDto createExcelDto(
            List<? extends ExcelRowConvertor> data, Class<? extends ExcelRowConvertor> clazz) {

        ExcelWriterDto dto = new ExcelWriterDto();
        dto.setFileName(getFileName(clazz));
        dto.setHeadList(ExcelUtils.getHeaderList(clazz));
        dto.setBodyList(data.stream().map(ExcelRowConvertor::convertToExcelFormat).toList());
        dto.setStyle(getStyleEnabled(clazz));

        String sheetName = getSheetName(clazz);
        if (StringUtils.hasText(sheetName)) {
            dto.setSheetName(sheetName);
        }

        return dto;
    }

    private Workbook createWorkbookInstance(Class<? extends Workbook> workbookClass) {
        return HSSFWorkbook.class.isAssignableFrom(workbookClass)
                ? new HSSFWorkbook()
                : new XSSFWorkbook();
    }

    private void setupSheet(Workbook wb, ExcelWriterDto excelDto) {
        Sheet sheet =
                StringUtils.hasText(excelDto.getSheetName())
                        ? wb.createSheet(excelDto.getSheetName())
                        : wb.createSheet();

        createHead(sheet, excelDto.getHeadList());
        createBody(sheet, excelDto.getBodyList());

        // 컬럼 너비 자동 조정
        for (int i = 0; i < excelDto.getHeadList().size(); i++) {
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 2048);
        }

        if (excelDto.isStyle()) {
            applyStyles(sheet, wb);
        }
    }

    private void createHead(Sheet sheet, List<String> headList) {
        createRow(sheet, headList, 0);
    }

    private void createBody(Sheet sheet, List<List<Object>> bodyList) {
        for (int i = 0; i < bodyList.size(); i++) {
            createRow(sheet, bodyList.get(i), i + 1);
        }
    }

    private void createRow(Sheet sheet, List<?> cellList, int rowNum) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < cellList.size(); i++) {
            setCellValue(row.createCell(i), cellList.get(i));
        }
    }

    private void setCellValue(Cell cell, Object value) {
        if (value instanceof String s) {
            cell.setCellValue(s);
        } else if (value instanceof Double d) {
            cell.setCellValue(d);
        } else if (value instanceof Integer i) {
            cell.setCellValue(i);
        } else if (value instanceof Long l) {
            cell.setCellValue(l);
        } else if (value instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (value instanceof Date d) {
            cell.setCellValue(d);
        } else if (value instanceof Calendar c) {
            cell.setCellValue(c);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
    }

    private String getFileExtension(String fileName, Workbook wb) {
        if (fileName == null) {
            fileName = "download";
        }

        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            fileName = fileName.substring(0, lastDot);
        }

        if (wb instanceof XSSFWorkbook || wb instanceof SXSSFWorkbook) {
            return fileName + ".xlsx";
        }
        if (wb instanceof HSSFWorkbook) {
            return fileName + ".xls";
        }

        return fileName + ".xlsx";
    }

    private String getFileName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(ExcelFile.class)) {
            String filename = clazz.getAnnotation(ExcelFile.class).filename();
            return StringUtils.hasText(filename) ? filename : clazz.getSimpleName();
        }
        return clazz.getSimpleName();
    }

    private boolean getStyleEnabled(Class<?> clazz) {
        if (clazz.isAnnotationPresent(ExcelSheet.class)) {
            return clazz.getAnnotation(ExcelSheet.class).style();
        }
        return true;
    }

    private String getSheetName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(ExcelSheet.class)) {
            return clazz.getAnnotation(ExcelSheet.class).sheetName();
        }
        return null;
    }

    /** 헤더 스타일 적용 (노란 배경, 굵은 글씨, 테두리) */
    private void applyStyles(Sheet sheet, Workbook wb) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return;
        }

        CellStyle headerStyle = wb.createCellStyle();
        createHeaderStyle(headerStyle, wb);

        CellStyle bodyStyle = wb.createCellStyle();
        setBorders(bodyStyle);

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellStyle(headerStyle);
        }

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cell.setCellStyle(bodyStyle);
                }
            }
        }
    }

    private void createHeaderStyle(CellStyle style, Workbook wb) {
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        style.setFont(headerFont);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
