package app.backend.core.base.component;

import java.util.List;

import org.springframework.http.HttpStatus;

import app.backend.core.base.vo.BizRespVo;
import app.backend.core.base.vo.GenericListVo;

/** 컨트롤러 공통 기능을 제공하는 추상 클래스 모든 컨트롤러는 이 클래스를 상속받아 일관된 응답 포맷을 사용합니다. */
public abstract class AbstractController {

    /**
     * 기본 성공 응답 (200 OK)
     *
     * @param data 응답 데이터
     * @return BizRespVo
     */
    protected final <T> BizRespVo<T> makeResponse(T data) {
        return BizRespVo.withStatus(HttpStatus.OK, data);
    }

    /**
     * 메시지를 포함한 성공 응답 (200 OK)
     *
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return BizRespVo
     */
    protected final <T> BizRespVo<T> makeResponse(String message, T data) {
        return BizRespVo.withStatus(HttpStatus.OK, message, data);
    }

    /**
     * 상태 코드를 지정한 응답
     *
     * @param status HTTP 상태 코드
     * @param data 응답 데이터
     * @return BizRespVo
     */
    protected final <T> BizRespVo<T> makeResponse(HttpStatus status, T data) {
        return BizRespVo.withStatus(status, data);
    }

    /**
     * 상태 코드와 메시지를 지정한 응답
     *
     * @param status HTTP 상태 코드
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return BizRespVo
     */
    protected final <T> BizRespVo<T> makeResponse(HttpStatus status, String message, T data) {
        return BizRespVo.withStatus(status, message, data);
    }

    /**
     * 리스트 응답 (200 OK)
     *
     * @param list 응답 리스트
     * @return BizRespVo with GenericListVo
     */
    protected <T> BizRespVo<GenericListVo<T>> makeGenericListResponse(List<T> list) {
        return makeResponse(new GenericListVo<>(list));
    }

    /**
     * 생성 성공 응답 (201 CREATED)
     *
     * @param data 응답 데이터
     * @return BizRespVo
     */
    protected final <T> BizRespVo<T> makeCreatedResponse(T data) {
        return BizRespVo.withStatus(HttpStatus.CREATED, data);
    }

    /**
     * 생성 성공 응답 with 메시지 (201 CREATED)
     *
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return BizRespVo
     */
    protected final <T> BizRespVo<T> makeCreatedResponse(String message, T data) {
        return BizRespVo.withStatus(HttpStatus.CREATED, message, data);
    }

    /**
     * 삭제 성공 응답 (200 OK, data는 null)
     *
     * @param message 응답 메시지
     * @return BizRespVo
     */
    protected final BizRespVo<Void> makeDeleteResponse(String message) {
        return BizRespVo.withStatus(HttpStatus.OK, message, null);
    }
}
