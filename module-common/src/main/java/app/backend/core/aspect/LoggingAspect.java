package app.backend.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 컨트롤러 메서드 실행 로깅 AOP 모든 컨트롤러 메서드 실행 시간을 측정합니다.
 *
 * <p>사용 예시: - DB에 로그 저장이 필요한 경우 이 클래스를 확장하여 사용 - 현재는 콘솔 로깅만 수행
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /** 모든 컨트롤러 메서드 실행 시간 측정 */
    @Around("execution(* app.backend.app..*Controller.*(..))")
    public Object logging(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed(pjp.getArgs());
            return result;
        } finally {
            long end = System.currentTimeMillis();
            String className = pjp.getTarget().getClass().getSimpleName();
            String methodName = pjp.getSignature().getName();

            log.debug(">> {}.{}() executed in {}ms", className, methodName, end - start);
        }
    }
}
