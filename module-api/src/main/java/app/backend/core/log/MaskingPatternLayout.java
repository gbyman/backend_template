package app.backend.core.log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback 마스킹 패턴 레이아웃 로그 메시지에서 민감 정보를 자동으로 마스킹 처리합니다.
 *
 * <p>사용 방법: logback-spring.xml에서 <encoder class="...MaskingPatternLayout">로 설정
 */
public class MaskingPatternLayout extends PatternLayout {

    private final List<MaskingPattern> maskingPatterns = new ArrayList<>();

    public MaskingPatternLayout() {
        // 마스킹 패턴 등록
        registerDefaultPatterns();
    }

    /** 기본 마스킹 패턴 등록 */
    private void registerDefaultPatterns() {
        // 1. 전화번호 마스킹
        // 010-1234-5678 → 010-****-5678
        maskingPatterns.add(
                new MaskingPattern(
                        "전화번호", Pattern.compile("(\\d{2,3})-?(\\d{3,4})-?(\\d{4})"), "$1-****-$3"));

        // 2. 이메일 마스킹
        // user@example.com → u***@example.com
        maskingPatterns.add(
                new MaskingPattern(
                        "이메일",
                        Pattern.compile("([a-zA-Z0-9._-])[a-zA-Z0-9._-]*@([a-zA-Z0-9.-]+)"),
                        "$1***@$2"));

        // 3. 비밀번호 마스킹 (JSON 형태)
        // "password":"mypass123" → "password":"*****"
        maskingPatterns.add(
                new MaskingPattern(
                        "비밀번호(JSON)",
                        Pattern.compile("(\"password\"\\s*:\\s*\")[^\"]*(\")"),
                        "$1*****$2"));

        // 4. 비밀번호 마스킹 (일반 텍스트)
        // password=mypass123 → password=*****
        maskingPatterns.add(
                new MaskingPattern(
                        "비밀번호(텍스트)", Pattern.compile("(password\\s*=\\s*)[^\\s,)]*"), "$1*****"));
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        // 원본 로그 메시지 가져오기
        String originalMessage = super.doLayout(event);

        // 마스킹 적용
        return maskMessage(originalMessage);
    }

    /** 메시지에 마스킹 패턴 적용 */
    private String maskMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String maskedMessage = message;

        // 등록된 모든 패턴 적용
        for (MaskingPattern pattern : maskingPatterns) {
            maskedMessage = pattern.mask(maskedMessage);
        }

        return maskedMessage;
    }

    /** 마스킹 패턴 정의 클래스 */
    private static class MaskingPattern {
        private final String name;
        private final Pattern pattern;
        private final String replacement;

        public MaskingPattern(String name, Pattern pattern, String replacement) {
            this.name = name;
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public String mask(String message) {
            Matcher matcher = pattern.matcher(message);
            return matcher.replaceAll(replacement);
        }

        public String getName() {
            return name;
        }
    }

    /** 런타임에 커스텀 패턴 추가 (선택적) */
    public void addMaskingPattern(String name, String regex, String replacement) {
        maskingPatterns.add(new MaskingPattern(name, Pattern.compile(regex), replacement));
    }
}
