package app.backend.core.utils;

import org.springframework.util.StringUtils;

import lombok.experimental.UtilityClass;

/** 민감정보 마스킹 유틸리티 */
@UtilityClass
public class MaskingUtils {

    private static final int IPV6_PARTS_COUNT = 8;
    private static final int IPV6_LAST_INDEX = 7;
    private static final int IPV4_PARTS_COUNT = 4;
    private static final int PHONE_DIGITS_LENGTH = 11;
    private static final int PHONE_MIDDLE_START = 3;
    private static final int PHONE_MIDDLE_END = 7;
    private static final int PHONE_PARTS_COUNT = 3;

    /**
     * IP 주소 마스킹
     *
     * <p>17~24비트 영역 (Ver4) 113~128비트 영역 (Ver6)
     *
     * <p>예) (Ver4) 123.123.***.123 (Ver6) 21DA:00D3:0000:2F3B:02AA:00FF:FE28:****
     */
    public String maskIp(String ip) {
        if (ip.contains(".")) {
            return maskIpv4(ip);
        } else if (ip.contains(":")) {
            return maskIpv6(ip);
        } else {
            return ip;
        }
    }

    private String maskIpv6(String ip) {
        if (!StringUtils.hasText(ip)) {
            return ip;
        }

        String[] parts = ip.split(":");

        if (parts.length != IPV6_PARTS_COUNT) {
            return ip;
        }

        parts[IPV6_LAST_INDEX] = "****";

        return String.join(":", parts);
    }

    private String maskIpv4(String ip) {
        if (!StringUtils.hasText(ip)) {
            return ip;
        }

        String[] parts = ip.split("\\.");

        if (parts.length != IPV4_PARTS_COUNT) {
            return ip;
        }

        parts[2] = "***";

        return String.join(".", parts);
    }

    /**
     * 이메일 주소 마스킹
     *
     * <p>ID 중 앞 2자리를 제외한 나머지 (아이디가 2자리인 경우 두번째 글자 *, 1자리는 모두 *) 예) hy******@example.com
     */
    public String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }

        String[] parts = email.split("@");

        if (parts.length != 2) {
            return email;
        }

        String localPart = parts[0];
        String domainParts = parts[1];

        if (localPart.length() <= 2) {
            return localPart + "@" + domainParts;
        }

        String visiblePart = localPart.substring(0, 2);
        String maskedPart = "*".repeat(localPart.length() - 2);

        return visiblePart + maskedPart + "@" + domainParts;
    }

    /**
     * 성명 마스킹
     *
     * <p>성명 중 이름의 첫 번째 글자 이상 (영문성명 중 앞 4자리 철자 노출) 예) 손*민 (SON **********)
     */
    public String maskName(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }

        if (name.length() <= 2) {
            return name.charAt(0) + "*";
        }

        String firstChar = String.valueOf(name.charAt(0));
        String lastChar = String.valueOf(name.charAt(name.length() - 1));
        String maskedMiddle = "*".repeat(name.length() - 2);

        return firstChar + maskedMiddle + lastChar;
    }

    /**
     * 온라인 회원 ID 마스킹
     *
     * <p>ID 중 앞 2자리를 제외한 나머지 (아이디가 2자리인 경우 두번째 글자 *, 1자리는 모두 *) 예) hy******
     */
    public String maskId(String id) {
        if (!StringUtils.hasText(id)) {
            return id;
        }

        if (id.length() <= 2) {
            return id;
        }

        String visiblePart = id.substring(0, 2);
        String maskPart = "*".repeat(id.length() - 2);

        return visiblePart + maskPart;
    }

    /**
     * 전화번호 마스킹
     *
     * <p>중간 4자리 마스킹 예) 010-****-5678
     */
    public String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }

        // 010-1234-5678 형식
        if (phone.contains("-")) {
            String[] parts = phone.split("-");
            if (parts.length == PHONE_PARTS_COUNT) {
                return parts[0] + "-****-" + parts[2];
            }
        }

        // 01012345678 형식
        if (phone.length() == PHONE_DIGITS_LENGTH) {
            return phone.substring(0, PHONE_MIDDLE_START)
                    + "****"
                    + phone.substring(PHONE_MIDDLE_END);
        }

        return phone;
    }
}
