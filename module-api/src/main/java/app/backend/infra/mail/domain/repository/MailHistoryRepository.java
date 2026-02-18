package app.backend.infra.mail.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.backend.infra.mail.domain.entity.MailHistoryEntity;

/** 메일 발송 이력 Repository */
public interface MailHistoryRepository extends JpaRepository<MailHistoryEntity, Long> {

    /** 발송 상태별 조회 */
    List<MailHistoryEntity> findByStatus(MailHistoryEntity.MailStatus status);

    /** 기간별 조회 */
    List<MailHistoryEntity> findBySentAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /** 수신자 이메일로 조회 */
    List<MailHistoryEntity> findByToAddress(String toAddress);
}
