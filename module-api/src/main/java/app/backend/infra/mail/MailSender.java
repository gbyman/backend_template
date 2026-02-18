package app.backend.infra.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;

/** 메일 발송 인터페이스 구현체: JMailSender (JavaMail) */
public interface MailSender {
    /**
     * 메일 발송
     *
     * @param subject 메일 제목
     * @param content 메일 내용
     * @param toAddress 수신자
     * @param cc 참조 (nullable)
     * @param bcc 숨은 참조 (nullable)
     * @param isHtml HTML 여부
     * @throws MessagingException 메일 발송 실패 시
     * @throws UnsupportedEncodingException 인코딩 오류 시
     */
    void sendMail(
            String subject,
            String content,
            String toAddress,
            String[] cc,
            String[] bcc,
            boolean isHtml)
            throws MessagingException, UnsupportedEncodingException;

    /**
     * 첨부파일 포함 메일 발송
     *
     * @param subject 메일 제목
     * @param content 메일 내용 (HTML의 경우 Base64 이미지가 자동으로 CID 인라인 변환됨)
     * @param toAddress 수신자
     * @param cc 참조 (nullable)
     * @param bcc 숨은 참조 (nullable)
     * @param isHtml HTML 여부
     * @param attachments 첨부파일 (nullable)
     */
    default void sendMail(
            String subject,
            String content,
            String toAddress,
            String[] cc,
            String[] bcc,
            boolean isHtml,
            File... attachments)
            throws MessagingException, UnsupportedEncodingException {
        sendMail(subject, content, toAddress, cc, bcc, isHtml);
    }
}
