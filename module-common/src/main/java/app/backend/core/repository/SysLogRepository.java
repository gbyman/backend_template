package app.backend.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.backend.core.entity.SysLog;

/**
 * 시스템 로그 Repository
 *
 * <p>시스템 접근 로그를 DB에 저장하고 조회합니다.
 *
 * <p><strong>주요 기능:</strong>
 *
 * <ul>
 *   <li>로그 저장 (자동 - DatabaseLoggingAspect에서 호출)
 *   <li>로그 조회 (필요 시 커스텀 쿼리 추가)
 * </ul>
 *
 * <p><strong>인덱스 전략:</strong>
 *
 * <ul>
 *   <li>PK만 사용 (LOG_ID, REQUEST_TIME) - INSERT 성능 우선
 *   <li>에러 추적은 MDC + 로그 파일 사용 권장
 *   <li>필요 시 나중에 인덱스 추가 (docs/db/tb_sys_log.sql 참고)
 * </ul>
 *
 * <p><strong>사용 예시 (커스텀 쿼리 추가 시):</strong>
 *
 * <pre>
 * // 특정 사용자의 최근 로그 조회
 * List&lt;SysLog&gt; logs = sysLogRepository
 *     .findByUserIdOrderByRequestTimeDesc("user123", PageRequest.of(0, 10));
 *
 * // 특정 기간 로그 조회
 * List&lt;SysLog&gt; logs = sysLogRepository
 *     .findByRequestTimeBetween(startTime, endTime);
 *
 * // 에러 로그 조회 (인덱스 추가 후 사용 권장)
 * List&lt;SysLog&gt; errorLogs = sysLogRepository
 *     .findByStatusCodeGreaterThanEqualAndRequestTimeBetween(400, startTime, endTime);
 * </pre>
 *
 * @see app.backend.core.entity.SysLog
 * @see app.backend.core.aspect.DatabaseLoggingAspect
 */
@Repository
public interface SysLogRepository extends JpaRepository<SysLog, Long> {

    // 필요한 경우 커스텀 쿼리 메서드 추가
    // 예:
    // List<SysLog> findByUserIdOrderByRequestTimeDesc(String userId, Pageable pageable);
    // List<SysLog> findByRequestTimeBetween(LocalDateTime start, LocalDateTime end);
    // List<SysLog> findByStatusCodeGreaterThanEqualAndRequestTimeBetween(
    //     Integer statusCode, LocalDateTime start, LocalDateTime end);
    // List<SysLog> findByLogCategoryAndRequestTimeBetween(
    //     LogCategory category, LocalDateTime start, LocalDateTime end);
}
