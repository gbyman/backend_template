package app.backend.core.base.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonFormat;

import app.backend.core.utils.IpUtils;
import app.backend.core.utils.UuidUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class ReqContextVo extends BaseVo {

    private String requestId;
    private String requestUri;
    private String clientIp;
    private HttpMethod httpMethod;
    private String langDivVal;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTimestamp;

    public static ReqContextVo from(
            HttpServletRequest request, String defaultLang, Set<String> supportedLangs) {
        return new ReqContextVo(
                request.getRequestURI(),
                IpUtils.getClientIp(request),
                HttpMethod.valueOf(request.getMethod()),
                resolveLanguage(
                        request.getHeader(HttpHeaders.ACCEPT_LANGUAGE),
                        defaultLang,
                        supportedLangs));
    }

    private ReqContextVo(
            String requestUri, String clientIp, HttpMethod httpMethod, String langDivVal) {

        this.requestId = UuidUtils.generate();
        this.requestTimestamp = LocalDateTime.now();
        this.requestUri = requestUri;
        this.clientIp = clientIp;
        this.httpMethod = httpMethod;
        this.langDivVal = langDivVal;
    }

    /**
     * Accept-Language 헤더를 파싱하여 ISO 639-1 소문자 언어 코드로 변환
     *
     * <p>예: "ko-KR,ko;q=0.9,en-US;q=0.8" → "ko"
     *
     * <p>예: "en" → "en"
     *
     * <p>지원하지 않는 언어이거나 헤더가 없으면 기본값 반환
     */
    private static String resolveLanguage(
            String acceptLanguage, String defaultLang, Set<String> supportedLangs) {
        if (StringUtils.isBlank(acceptLanguage)) {
            return defaultLang;
        }

        try {
            List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLanguage);
            for (Locale.LanguageRange range : ranges) {
                String lang = range.getRange().split("-")[0].toLowerCase();
                if (supportedLangs.contains(lang)) {
                    return lang;
                }
            }
        } catch (IllegalArgumentException e) {
            // 잘못된 Accept-Language 형식 → 기본값 반환
        }

        return defaultLang;
    }
}
