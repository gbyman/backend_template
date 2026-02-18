package app.backend.core.utils.excel.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ExcelWriter 내부 전달 DTO */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelWriterDto {
    private String fileName;
    private String sheetName;
    private boolean style;
    private List<String> headList;
    private List<List<Object>> bodyList;
}
