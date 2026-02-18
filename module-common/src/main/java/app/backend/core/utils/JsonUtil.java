package app.backend.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JsonUtil {

    // 정규식 패턴을 미리 컴파일
    private static final Pattern MARKDOWN_JSON_PATTERN =
            Pattern.compile("```json\\s*\\n([\\s\\S]*?)\\n```");

    private static final Pattern SIMPLE_JSON_PATTERN = Pattern.compile("(\\{[\\s\\S]*\\})");

    public <R> R convertDtoFromJson(String target, Class<R> responseClass)
            throws JsonProcessingException {

        if (StringUtils.isBlank(target)) {
            throw new IllegalArgumentException("JSON 문자열이 비어있습니다.");
        }

        // json 추출
        String jsonStr = extractJsonFromText(target);

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(jsonStr, responseClass);
    }

    public <R> R convertDtoFromJson(String target, TypeReference<R> typeReference)
            throws JsonProcessingException {
        if (StringUtils.isBlank(target)) {
            throw new IllegalArgumentException("JSON 문자열이 비어있습니다.");
        }

        String jsonStr = extractJsonFromText(target);

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(jsonStr, typeReference);
    }

    /** 텍스트에서 JSON 부분만 추출하는 메서드 */
    public String extractJsonFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("추출할 텍스트가 비어있습니다.");
        }

        // 1. 마크다운 코드 블록에서 JSON 추출 시도
        Matcher matcher = MARKDOWN_JSON_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 2. 단순 JSON 객체 추출 시도
        matcher = SIMPLE_JSON_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 3. 마지막으로 전체 텍스트가 JSON인지 확인
        String trimmedText = text.trim();
        if (trimmedText.startsWith("{") && trimmedText.endsWith("}")) {
            return trimmedText;
        }

        log.error("JSON 추출 실패. 원본 텍스트: {}", text);

        throw new RuntimeException("텍스트에서 JSON을 찾을 수 없습니다. 응답 형식을 확인해주세요.");
    }
}
