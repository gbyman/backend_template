package app.backend.core.jwt.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.backend.core.base.exception.BizException;
import app.backend.core.base.vo.BizErrorVo;
import app.backend.core.base.vo.BizRespVo;
import app.backend.core.constants.MessageConstants;
import app.backend.core.utils.ResponseUtils;
import app.backend.core.utils.ServletUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/** JWT Exception Handler Filter. JWT 필터 처리 중 예외 발생 시 전역 예외 처리로 전달되지 않으므로 별도 필터로 처리. */
@Slf4j
@Component
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final boolean debug;

    public JwtExceptionHandlerFilter(
            ObjectMapper objectMapper, @Value("${spring.application.debug:false}") boolean debug) {
        this.objectMapper = objectMapper;
        this.debug = debug;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleException(response, HttpStatus.UNAUTHORIZED, MessageConstants.TOKEN_EXPIRED, e);
        } catch (JwtException | IllegalArgumentException e) {
            handleException(response, HttpStatus.UNAUTHORIZED, MessageConstants.UNAUTHORIZED, e);
        } catch (AccessDeniedException e) {
            handleException(response, HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (LockedException e) {
            handleException(
                    response, HttpStatus.FORBIDDEN, MessageConstants.IS_DISABLED_ACCOUNT, e);
        } catch (AccountExpiredException e) {
            handleException(response, HttpStatus.FORBIDDEN, MessageConstants.ACCOUNT_WITHDRAWN, e);
        } catch (BizException e) {
            handleException(response, e.getStatus(), e.getErrorCode(), e);
        }
    }

    private void handleException(
            HttpServletResponse response,
            HttpStatus status,
            String errorCode,
            Throwable exception) {

        log.error(
                ">>>> jwt Exception: [{}] {}, uri={}",
                exception.getClass().getSimpleName(),
                exception.getMessage() != null ? exception.getMessage() : "(no message)",
                ServletUtils.getServletRequest().getRequestURI());

        BizRespVo<BizErrorVo> errorBody =
                ResponseUtils.makeErrorResponse(status, errorCode, "", exception, debug);

        try {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
        } catch (IOException ioException) {
            log.error("Failed to write error response: {}", ioException.getMessage(), ioException);
        }
    }
}
