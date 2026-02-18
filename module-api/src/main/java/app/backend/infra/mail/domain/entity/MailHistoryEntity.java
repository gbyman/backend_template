package app.backend.infra.mail.domain.entity;

import java.time.LocalDateTime;

import app.backend.core.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 메일 발송 이력 엔티티 */
@Entity
@Table(name = "TB_MAIL_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MAIL_HISTORY_ID")
    private Long id;

    /** 메일 제목 */
    @Column(name = "SUBJECT", nullable = false, length = 500)
    private String subject;

    /** 메일 내용 */
    @Column(name = "CONTENT", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 수신자 이메일 */
    @Column(name = "TO_ADDRESS", nullable = false, length = 500)
    private String toAddress;

    /** 참조 이메일 (콤마 구분) */
    @Column(name = "CC", length = 1000)
    private String cc;

    /** 숨은 참조 이메일 (콤마 구분) */
    @Column(name = "BCC", length = 1000)
    private String bcc;

    /** HTML 여부 */
    @Column(name = "IS_HTML", nullable = false)
    private Boolean isHtml;

    /** 발송 상태 (SUCCESS, FAILED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private MailStatus status;

    /** 에러 메시지 (실패 시) */
    @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;

    /** 발송 시도 시각 */
    @Column(name = "SENT_AT", nullable = false)
    private LocalDateTime sentAt;

    @Builder
    @SuppressWarnings("checkstyle:ParameterNumber")
    public MailHistoryEntity(
            String subject,
            String content,
            String toAddress,
            String cc,
            String bcc,
            Boolean isHtml,
            MailStatus status,
            String errorMessage,
            LocalDateTime sentAt) {
        this.subject = subject;
        this.content = content;
        this.toAddress = toAddress;
        this.cc = cc;
        this.bcc = bcc;
        this.isHtml = isHtml;
        this.status = status;
        this.errorMessage = errorMessage;
        this.sentAt = sentAt;
    }

    /** 메일 발송 상태 */
    public enum MailStatus {
        SUCCESS, // 발송 성공
        FAILED // 발송 실패
    }
}
