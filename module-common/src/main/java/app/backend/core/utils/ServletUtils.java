package app.backend.core.utils;

import java.util.Optional;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import app.backend.core.base.vo.ReqContextVo;
import app.backend.core.constants.RequestAttributeKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServletUtils {
    public HttpServletRequest getServletRequest() {
        return Optional.of(RequestContextHolder.getRequestAttributes())
                .map(ra -> ((ServletRequestAttributes) ra).getRequest())
                .orElse(null);
    }

    public HttpServletResponse getServletResponse() {
        return Optional.of(RequestContextHolder.getRequestAttributes())
                .map(ra -> ((ServletRequestAttributes) ra).getResponse())
                .orElse(null);
    }

    public String getRequestURI() {
        return Optional.ofNullable(getServletRequest())
                .map(HttpServletRequest::getRequestURI)
                .orElse(null);
    }

    public String getHeader(String key) {
        return Optional.ofNullable(getServletRequest()).map(req -> req.getHeader(key)).orElse(null);
    }

    public ReqContextVo getServletRequestContextVo() {
        return (ReqContextVo)
                Optional.ofNullable(getServletRequest())
                        .map(req -> req.getAttribute(RequestAttributeKey.REQUEST_CONTEXT.name()))
                        .orElse(null);
    }
}
