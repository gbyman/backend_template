package app.backend.core.validator;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import app.backend.core.annotation.NoXSS;
import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** XSS(Cross-Site Scripting) 공격 패턴 차단 Validator 사용자 입력에서 위험한 스크립트 패턴을 감지하고 차단합니다. */
public class XSSValidator implements ConstraintValidator<NoXSS, String> {

    private static final int MAX_PATTERN_DISPLAY_LENGTH = 50;

    // XSS 공격 패턴 정규식
    private static final Pattern[] XSS_PATTERNS = {
        // <script> 태그
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),

        // javascript: URL
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),

        // on* 이벤트 핸들러
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),

        // <iframe> 태그
        Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<iframe[^>]*>", Pattern.CASE_INSENSITIVE),

        // <embed> 태그
        Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE),

        // <object> 태그
        Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<object[^>]*>", Pattern.CASE_INSENSITIVE),

        // eval() 함수
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),

        // expression() (CSS)
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),

        // vbscript: URL
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),

        // <img> 태그의 onerror
        Pattern.compile("<img[^>]+onerror", Pattern.CASE_INSENSITIVE),

        // <svg> 태그의 onload
        Pattern.compile("<svg[^>]+onload", Pattern.CASE_INSENSITIVE),

        // data: URL with base64
        Pattern.compile("data:text/html", Pattern.CASE_INSENSITIVE),

        // <link> 태그
        Pattern.compile("<link[^>]*>", Pattern.CASE_INSENSITIVE),

        // <meta> 태그의 refresh
        Pattern.compile("<meta[^>]+http-equiv[^>]+refresh", Pattern.CASE_INSENSITIVE),

        // document.cookie
        Pattern.compile("document\\.cookie", Pattern.CASE_INSENSITIVE),

        // window.location
        Pattern.compile("window\\.location", Pattern.CASE_INSENSITIVE),

        // <form> 태그
        Pattern.compile("<form[^>]*>", Pattern.CASE_INSENSITIVE),

        // <input> 태그
        Pattern.compile("<input[^>]*>", Pattern.CASE_INSENSITIVE),

        // <base> 태그
        Pattern.compile("<base[^>]*>", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이거나 비어있으면 통과 (required 체크는 @NotNull, @NotBlank로)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // XSS 패턴 검사
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                // 어떤 패턴이 감지되었는지 로깅용으로 추출
                String detectedPattern = extractDetectedPattern(value, pattern);

                throw new BizException(
                        HttpStatus.BAD_REQUEST,
                        MessageConstants.BAD_REQUEST,
                        String.format(
                                "입력값에 위험한 스크립트 패턴이 포함되어 있습니다. (감지된 패턴: %s)",
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
