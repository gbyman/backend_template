package app.backend.core.exception;

import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.backend.core.base.exception.BizException;
import app.backend.core.base.exception.ExcelUploadException;
import app.backend.core.base.vo.BizErrorVo;
import app.backend.core.base.vo.BizRespVo;
import app.backend.core.constants.MessageConstants;
import app.backend.core.exception.constants.ErrorCode;
import app.backend.core.ratelimit.TooManyRequestsException;
import app.backend.core.utils.ResponseUtils;
import app.backend.core.utils.excel.vo.ExcelErrorRespVo;
import jakarta.annotation.Nullable;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Value("${spring.error.response.include-stack-trace:false}")
    private boolean includeStackTrace;

    private final MessageSource messageSource;

    public String getMessageFromProperties(ErrorCode errorCode, @Nullable Object[] args) {
        String defaultMessage = "시스템 처리 중 에러가 발생했습니다.";
        String result =
                messageSource.getMessage(
                        errorCode.getErrorCode(), args, defaultMessage, Locale.getDefault());

        if (StringUtils.isBlank(result)) {
            log.warn(">> message code 없음: {}", errorCode.getErrorCode());
        }

        return result;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BizRespVo<BizErrorVo> exception(Exception ex) {
        setErrorMDC(HttpStatus.INTERNAL_SERVER_ERROR, ex);
        log.error(">>> error: {}", ex.getMessage(), ex);

        return ResponseUtils.makeErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                MessageConstants.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMlgCode(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex,
                includeStackTrace);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public BizRespVo<BizErrorVo> accessDeniedException(AccessDeniedException ex) {
        setErrorMDC(HttpStatus.FORBIDDEN, ex);
        log.error(">>> error: {}", ex.getMessage(), ex);

        return ResponseUtils.makeErrorResponse(
                HttpStatus.FORBIDDEN,
                MessageConstants.FORBIDDEN,
                ErrorCode.FORBIDDEN.getMlgCode(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex,
                includeStackTrace);
    }

    /**
     * Rate Limit 초과 예외 처리
     *
     * <p>HTTP 429 Too Many Requests 응답
     */
    @ExceptionHandler(TooManyRequestsException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public BizRespVo<BizErrorVo> tooManyRequestsException(TooManyRequestsException ex) {
        setErrorMDC(HttpStatus.TOO_MANY_REQUESTS, ex);
        log.warn(">>> rate limit exceeded: {}", ex.getMessage());

        return ResponseUtils.makeErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                "TOO_MANY_REQUESTS",
                ex.getMessage(),
                ex,
                false // Rate Limit은 스택 트레이스 불필요
                );
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<BizRespVo<BizErrorVo>> bizException(BizException ex) {
        setErrorMDC(ex.getStatus(), ex);
        log.error(">>> error: {}", ex.getMessage(), ex);

        BizRespVo<BizErrorVo> body =
                ResponseUtils.makeErrorResponse(
                        ex.getStatus(), ex.getErrorCode(), ex.getMessage(), ex, includeStackTrace);

        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BizRespVo<BizErrorVo> methodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        String errorMessage = extractBindingErrors(ex);
        setErrorMDC(HttpStatus.BAD_REQUEST, ex, errorMessage);
        log.error(">>> validation error: {}", errorMessage, ex);

        return ResponseUtils.makeErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.BAD_REQUEST,
                ErrorCode.BAD_REQUEST.getMlgCode(),
                errorMessage,
                ex,
                includeStackTrace);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BizRespVo<BizErrorVo> bindException(BindException ex) {

        String errorMessage = extractBindingErrors(ex);
        setErrorMDC(HttpStatus.BAD_REQUEST, ex, errorMessage);
        log.error(">>> bind error: {}", errorMessage, ex);

        return ResponseUtils.makeErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.BAD_REQUEST,
                ErrorCode.BAD_REQUEST.getMlgCode(),
                errorMessage,
                ex,
                includeStackTrace);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BizRespVo<BizErrorVo> validationException(ValidationException ex) {

        // ValidationException 내부에 BizException이 있는지 확인
        Throwable cause = ex.getCause();
        if (cause instanceof BizException bizException) {
            setErrorMDC(bizException.getStatus(), bizException);
            log.error(
                    ">>> validation error (BizException): {}",
                    bizException.getMessage(),
                    bizException);

            return ResponseUtils.makeErrorResponse(
                    bizException.getStatus(),
                    bizException.getErrorCode(),
                    bizException.getMessage(),
                    bizException,
                    includeStackTrace);
        }

        setErrorMDC(HttpStatus.BAD_REQUEST, ex);
        log.error(">>> validation error: {}", ex.getMessage(), ex);

        return ResponseUtils.makeErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.BAD_REQUEST,
                ErrorCode.BAD_REQUEST.getMlgCode(),
                ex.getMessage(),
                ex,
                includeStackTrace);
    }

    /** 엑셀 업로드 검증 에러 처리 */
    @ExceptionHandler(ExcelUploadException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BizRespVo<ExcelErrorRespVo> excelUploadException(ExcelUploadException ex) {
        setErrorMDC(HttpStatus.BAD_REQUEST, ex);
        log.warn(">>> excel upload error: {}건", ex.getErrors().size());

        ExcelErrorRespVo errorResp = new ExcelErrorRespVo(ex.getErrors());
        return ResponseUtils.makeResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), errorResp);
    }

    /** BindingResult에서 에러 메시지 추출 */
    private String extractBindingErrors(BindException ex) {
        return ex.getBindingResult().getAllErrors().stream()
                .map(
                        error -> {
                            if (error instanceof FieldError fieldError) {
                                return String.format(
                                        "%s: %s", fieldError.getField(), error.getDefaultMessage());
                            }
                            return error.getDefaultMessage();
                        })
                .collect(Collectors.joining(", "));
    }

    /** MDC에 에러 정보 설정 (Elasticsearch 로깅용) */
    private void setErrorMDC(HttpStatus status, Throwable ex) {
        setErrorMDC(status, ex, ex.getMessage());
    }

    /** MDC에 에러 정보 설정 (커스텀 메시지 포함) */
    private void setErrorMDC(HttpStatus status, Throwable ex, String customMessage) {
        MDC.put("errorStatus", String.valueOf(status.value()));
        MDC.put("errorCode", status.name());
        MDC.put("errorMessage", customMessage != null ? customMessage : ex.getMessage());
        MDC.put("errorClass", ex.getClass().getSimpleName());

        // BizException인 경우 추가 정보
        if (ex instanceof BizException bizEx) {
            MDC.put("bizErrorCode", bizEx.getErrorCode());
        }
    }
}
