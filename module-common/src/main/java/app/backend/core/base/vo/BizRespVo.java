package app.backend.core.base.vo;

import java.io.Serial;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import app.backend.core.utils.ReqContextUtils;
import app.backend.core.utils.UuidUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BizRespVo<T> extends BaseVo {

    @Serial private static final long serialVersionUID = -1297884405516187541L;

    @Schema(description = "HTTP Status Code", example = "OK")
    private final String statusCode;

    @Schema(description = "결과코드", example = "200")
    private final Integer resultCode;

    @Schema(description = "메시지")
    private final String message;

    @Schema(description = "바디")
    private final T body;

    @Schema(description = "요청 아이디")
    private final String requestId;

    @Schema(description = "Timestamp of response", example = "2025-01-01 12:34:56:789")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "Asia/Seoul")
    private final LocalDateTime timestamp;

    @JsonIgnore
    @Schema(hidden = true)
    private final ReqContextVo contextVo;

    public static <T> BizRespVo<T> withStatus(@NonNull HttpStatus status, T data) {

        return withStatus(status, status.getReasonPhrase(), data);
    }

    public static <T> BizRespVo<T> withStatus(@NonNull HttpStatus status, String message, T data) {

        ReqContextVo reqContext = ReqContextUtils.getReqContext();

        String requestId = reqContext != null ? reqContext.getRequestId() : UuidUtils.generate();

        return new BizRespVo<>(
                status.getReasonPhrase(),
                status.value(),
                message,
                data,
                requestId,
                LocalDateTime.now(),
                reqContext);
    }
}
