package app.backend.core.validator;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import app.backend.core.annotation.NoSQLInjection;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** SQL Injection 공격 패턴 차단 Validator 사용자 입력에서 SQL 인젝션 패턴을 감지하고 차단합니다. */
public class SQLInjectionValidator implements ConstraintValidator<NoSQLInjection, String> {

    private static final int MAX_PATTERN_DISPLAY_LENGTH = 30;

    // SQL Injection 공격 패턴 정규식
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        // SQL 주석
        Pattern.compile("--", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/\\*.*?\\*/", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),

        // SQL 키워드 (단독 또는 특수문자와 함께)
        Pattern.compile(
                "\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|EXECUTE|UNION|TRUNCATE)\\b",
                Pattern.CASE_INSENSITIVE),

        // SQL 함수
        Pattern.compile(
                "\\b(CONCAT|CHAR|ASCII|SUBSTRING|SUBSTR|SLEEP|BENCHMARK)\\s*\\(",
                Pattern.CASE_INSENSITIVE),

        // 저장 프로시저
        Pattern.compile("\\bsp_\\w+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bxp_\\w+", Pattern.CASE_INSENSITIVE),

        // 특수 문자 조합
        Pattern.compile("';\\s*--"), // '; --
        Pattern.compile("'\\s*OR\\s*'", Pattern.CASE_INSENSITIVE), // ' OR '
        Pattern.compile("'\\s*OR\\s*1\\s*=\\s*1", Pattern.CASE_INSENSITIVE), // ' OR 1=1
        Pattern.compile("'\\s*AND\\s*'", Pattern.CASE_INSENSITIVE), // ' AND '
        Pattern.compile("'\\s*=\\s*'", Pattern.CASE_INSENSITIVE), // ' = '

        // 세미콜론으로 여러 쿼리 실행
        Pattern.compile(";\\s*(SELECT|INSERT|UPDATE|DELETE|DROP)", Pattern.CASE_INSENSITIVE),

        // UNION 기반 공격
        Pattern.compile("\\bUNION\\b.*\\bSELECT\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),

        // 시간 기반 블라인드 SQL 인젝션
        Pattern.compile("\\b(WAITFOR|DELAY|SLEEP|BENCHMARK)\\s*\\(", Pattern.CASE_INSENSITIVE),

        // 불린 기반 블라인드 SQL 인젝션
        Pattern.compile("\\b(AND|OR)\\b\\s*\\d+\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE),

        // 정보 스키마 접근
        Pattern.compile("\\bINFORMATION_SCHEMA\\b", Pattern.CASE_INSENSITIVE),

        // 시스템 테이블 접근
        Pattern.compile("\\b(sys\\.|sysobjects|syscolumns)", Pattern.CASE_INSENSITIVE),

        // 16진수 인코딩
        Pattern.compile("0x[0-9a-f]+", Pattern.CASE_INSENSITIVE),

        // CAST/CONVERT 함수
        Pattern.compile("\\b(CAST|CONVERT)\\s*\\(", Pattern.CASE_INSENSITIVE),

        // INTO OUTFILE/DUMPFILE
        Pattern.compile("\\bINTO\\s+(OUTFILE|DUMPFILE)", Pattern.CASE_INSENSITIVE),

        // LOAD_FILE 함수
        Pattern.compile("\\bLOAD_FILE\\s*\\(", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이거나 비어있으면 통과 (required 체크는 @NotNull, @NotBlank로)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // SQL Injection 패턴 검사
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(value).find()) {
                // 어떤 패턴이 감지되었는지 추출
                String detectedPattern = extractDetectedPattern(value, pattern);

                throw new BizException(
                        HttpStatus.BAD_REQUEST,
                        MessageConstants.BAD_REQUEST,
                        String.format(
                                "입력값에 SQL 인젝션 패턴이 포함되어 있습니다. (감지된 패턴: %s)",
                                detectedPattern != null ? detectedPattern : "알 수 없음"));
            }
        }

        return true;
    }

    /** 감지된 패턴 추출 (디버깅/로깅용) */
    private String extractDetectedPattern(String value, Pattern pattern) {
        var matcher = pattern.matcher(value);
        if (matcher.find()) {
            String detected = matcher.group();
            // 로그에 노출될 수 있으므로 길이 제한
            return detected.length() > MAX_PATTERN_DISPLAY_LENGTH
                    ? detected.substring(0, MAX_PATTERN_DISPLAY_LENGTH) + "..."
                    : detected;
        }
        return null;
    }
}
