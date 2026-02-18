package app.backend.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.http.HttpStatus;

import app.backend.core.base.vo.BizErrorVo;
import app.backend.core.base.vo.BizRespVo;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseUtils {
    public <T> BizRespVo<T> makeResponse(HttpStatus httpStatus, final T body) {

        return BizRespVo.withStatus(httpStatus, body);
    }

    public <T> BizRespVo<T> makeResponse(HttpStatus httpStatus, String message, final T body) {

        return BizRespVo.withStatus(httpStatus, message, body);
    }

    public BizRespVo<BizErrorVo> makeErrorResponse(
            HttpStatus httpStatus, String errorCode, String message, Throwable th, boolean debug) {

        String path = ServletUtils.getRequestURI();

        BizErrorVo bizErrorVo = makeErrorVo(errorCode, null, path, th, debug);

        return makeResponse(httpStatus, message, bizErrorVo);
    }

    public BizRespVo<BizErrorVo> makeErrorResponse(
            HttpStatus httpStatus,
            String errorCode,
            String mlgCode,
            String message,
            Throwable th,
            boolean debug) {

        String path = ServletUtils.getRequestURI();

        BizErrorVo bizErrorVo = makeErrorVo(errorCode, mlgCode, path, th, debug);

        return makeResponse(httpStatus, message, bizErrorVo);
    }

    public BizErrorVo makeErrorVo(
            String errorCode,
            String mlgCode,
            String path,
            Throwable th,
            boolean includeStackTrace) {

        String trace = includeStackTrace ? convertStackTrace(th) : "";
        return BizErrorVo.of(errorCode, mlgCode, path, trace);
    }

    public String convertStackTrace(Throwable th) {
        if (th == null) {
            return "";
        }

        StringWriter writer = new StringWriter();
        th.printStackTrace(new PrintWriter(writer));

        return writer.toString();
    }
}
