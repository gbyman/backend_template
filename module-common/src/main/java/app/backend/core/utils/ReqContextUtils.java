package app.backend.core.utils;

import java.util.Locale;

import app.backend.core.base.vo.ReqContextVo;
import app.backend.core.constants.RequestAttributeKey;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReqContextUtils {

    public ReqContextVo getReqContext() {
        HttpServletRequest request = ServletUtils.getServletRequest();

        if (request == null) {
            return null;
        }

        Object context = request.getAttribute(RequestAttributeKey.REQUEST_CONTEXT.name());

        if (context instanceof ReqContextVo) {
            return (ReqContextVo) context;
        }

        return null;
    }

    /**
     * 현재 요청의 언어 코드를 반환 (ISO 639-1 소문자)
     *
     * <p>컨텍스트가 없으면 기본값 "ko" 반환
     */
    public String getLangDivVal() {
        ReqContextVo context = getReqContext();
        return context != null ? context.getLangDivVal() : Locale.KOREAN.getLanguage();
    }
}
