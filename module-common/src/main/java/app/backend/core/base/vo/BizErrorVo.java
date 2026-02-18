package app.backend.core.base.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class BizErrorVo extends BaseVo {
    private final String errorCode;
    private final String mlgCode;
    private final String path;
    private final String trace;

    public static BizErrorVo of(String errorCode, String path, String trace) {
        return BizErrorVo.builder().errorCode(errorCode).path(path).trace(trace).build();
    }

    public static BizErrorVo of(String errorCode, String mlgCode, String path, String trace) {
        return BizErrorVo.builder()
                .errorCode(errorCode)
                .mlgCode(mlgCode)
                .path(path)
                .trace(trace)
                .build();
    }
}
