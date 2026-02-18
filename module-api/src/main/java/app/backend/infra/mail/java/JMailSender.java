package app.backend.infra.mail.java;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import app.backend.infra.mail.MailSender;
import app.backend.infra.mail.domain.entity.MailHistoryEntity;
import app.backend.infra.mail.domain.repository.MailHistoryRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * JavaMail 기반 메일 발송 구현체
 *
 * <p>필요한 의존성: implementation 'org.springframework.boot:spring-boot-starter-mail'
 *
 * <p>설정 예시: spring: mail: host: smtp.gmail.com port: 587 username: ${MAIL_USERNAME} password:
 * ${MAIL_PASSWORD} properties: mail.smtp.auth: true mail.smtp.starttls.enable: true
 *
 * <p>mail: from-address: noreply@example.com
 */
@Slf4j
public class JMailSender implements MailSender {

    private final String fromAddress;

    private final JavaMailSender javaMailSender;

    private final MailHistoryRepository mailHistoryRepository;

    public JMailSender(
            String fromAddress,
            JavaMailSender javaMailSender,
            @Autowired(required = false) MailHistoryRepository mailHistoryRepository) {
        this.fromAddress = fromAddress;
        this.javaMailSender = javaMailSender;
        this.mailHistoryRepository = mailHistoryRepository;
    }

    @Override
    public void sendMail(
            String subject,
            String content,
            String toAddress,
            String[] cc,
            String[] bcc,
            boolean isHtml) {
        sendMail(subject, content, toAddress, cc, bcc, isHtml, (File[]) null);
    }

    @Override
    public void sendMail(
            String subject,
            String content,
            String toAddress,
            String[] cc,
            String[] bcc,
            boolean isHtml,
            File... attachments) {
        LocalDateTime sentAt = LocalDateTime.now();

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setSubject(encodeSubject(subject));
            helper.setFrom(
                    new InternetAddress(fromAddress, fromAddress, StandardCharsets.UTF_8.name()));
            helper.setTo(toAddress);

            // HTML 본문의 Base64 이미지를 CID 인라인 첨부로 변환
            List<InlineImage> inlineImages = new ArrayList<>();
            String processedContent = content;
            if (isHtml) {
                processedContent = extractAndEmbedBase64Images(content, inlineImages);
            }
            helper.setText(processedContent, isHtml);

            // Base64 인라인 이미지 첨부
            for (InlineImage img : inlineImages) {
                helper.addInline(img.contentId, new ByteArrayResource(img.data), img.mimeType);
            }

            // 파일 첨부
            if (attachments != null) {
                for (File attachment : attachments) {
                    if (attachment != null && attachment.exists()) {
                        helper.addAttachment(attachment.getName(), attachment);
                    }
                }
            }

            Optional.ofNullable(cc)
                    .filter(arr -> arr.length > 0)
                    .ifPresent(
                            addresses -> {
                                try {
                                    helper.setCc(addresses);
                                } catch (MessagingException e) {
                                    log.warn(">>> Failed to set CC addresses: {}", e.getMessage());
                                }
                            });

            Optional.ofNullable(bcc)
                    .filter(arr -> arr.length > 0)
                    .ifPresent(
                            addresses -> {
                                try {
                                    helper.setBcc(addresses);
                                } catch (MessagingException e) {
                                    log.warn(">>> Failed to set BCC addresses: {}", e.getMessage());
                                }
                            });

            javaMailSender.send(message);
            log.info(">>> 메일 발송 성공: to={}, subject={}", toAddress, subject);

            // 발송 성공 이력 저장
            saveMailHistory(
                    subject,
                    content,
                    toAddress,
                    cc,
                    bcc,
                    isHtml,
                    MailHistoryEntity.MailStatus.SUCCESS,
                    null,
                    sentAt);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error(">>> 메일 발송 실패: {}", e.getMessage(), e);

            // 발송 실패 이력 저장
            saveMailHistory(
                    subject,
                    content,
                    toAddress,
                    cc,
                    bcc,
                    isHtml,
                    MailHistoryEntity.MailStatus.FAILED,
                    e.getMessage(),
                    sentAt);

            throw new RuntimeException("메일 발송 중 오류가 발생했습니다.", e);
        }
    }

    private String encodeSubject(String subject) throws UnsupportedEncodingException {
        return MimeUtility.encodeText(subject, StandardCharsets.UTF_8.name(), "B");
    }

    /** 메일 발송 이력 저장 */
    @SuppressWarnings("checkstyle:ParameterNumber")
    private void saveMailHistory(
            String subject,
            String content,
            String toAddress,
            String[] cc,
            String[] bcc,
            boolean isHtml,
            MailHistoryEntity.MailStatus status,
            String errorMessage,
            LocalDateTime sentAt) {
        if (mailHistoryRepository == null) {
            log.debug(">>> MailHistoryRepository가 없어 이력 저장 생략");
            return;
        }

        try {
            MailHistoryEntity history =
                    MailHistoryEntity.builder()
                            .subject(subject)
                            .content(content)
                            .toAddress(toAddress)
                            .cc(arrayToString(cc))
                            .bcc(arrayToString(bcc))
                            .isHtml(isHtml)
                            .status(status)
                            .errorMessage(errorMessage)
                            .sentAt(sentAt)
                            .build();

            mailHistoryRepository.save(history);
            log.debug(">>> 메일 발송 이력 저장 완료: status={}, to={}", status, toAddress);

        } catch (Exception e) {
            // 이력 저장 실패는 메일 발송에 영향을 주지 않음
            log.error(">>> 메일 발송 이력 저장 실패: {}", e.getMessage(), e);
        }
    }

    /** 배열을 콤마로 구분된 문자열로 변환 */
    private String arrayToString(String[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return Stream.of(array).collect(Collectors.joining(", "));
    }

    private static final Pattern BASE64_IMG_PATTERN =
            Pattern.compile("src\\s*=\\s*\"data:(image/[^;]+);base64,([^\"]+)\"");

    /**
     * HTML 본문의 Base64 data URI 이미지를 CID 인라인 첨부로 변환
     *
     * <p>{@code <img src="data:image/png;base64,xxx">} → {@code <img src="cid:img_xxx">}
     */
    private String extractAndEmbedBase64Images(String html, List<InlineImage> inlineImages) {
        Matcher matcher = BASE64_IMG_PATTERN.matcher(html);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String mimeType = matcher.group(1);
            String base64Data = matcher.group(2);
            String contentId = "img_" + UUID.randomUUID().toString().substring(0, 8);

            try {
                byte[] imageData = Base64.getDecoder().decode(base64Data);
                inlineImages.add(new InlineImage(contentId, mimeType, imageData));
                matcher.appendReplacement(sb, "src=\"cid:" + contentId + "\"");
            } catch (IllegalArgumentException e) {
                log.warn(">>> Base64 이미지 디코딩 실패, 원본 유지: {}", e.getMessage());
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /** Base64 이미지 → CID 인라인 첨부 변환용 내부 DTO */
    private record InlineImage(String contentId, String mimeType, byte[] data) {}
}
