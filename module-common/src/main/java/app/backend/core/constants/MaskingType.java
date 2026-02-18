package app.backend.core.constants;

/** 마스킹 타입 */
public enum MaskingType {
    /** 이름: 손*민, SON ********** */
    NAME,

    /** 아이디: hy****** */
    ID,

    /** 이메일: hy******@example.com */
    EMAIL,

    /** IP 주소: 123.123.***.123 */
    IP,

    /** 전화번호: 010-****-5678 */
    PHONE
}
