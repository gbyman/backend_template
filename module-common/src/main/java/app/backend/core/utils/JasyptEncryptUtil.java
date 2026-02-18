package app.backend.core.utils;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Jasypt 암호화 유틸리티
 *
 * <p>사용 예시: public static void main(String[] args) { String secretKey = "your-secret-key"; String
 * plainText = "postgres";
 *
 * <p>String encrypted = JasyptEncryptUtil.encrypt(secretKey, plainText);
 * System.out.println("Encrypted: ENC(" + encrypted + ")");
 *
 * <p>String decrypted = JasyptEncryptUtil.decrypt(secretKey, encrypted);
 * System.out.println("Decrypted: " + decrypted); }
 */
public final class JasyptEncryptUtil {

    private JasyptEncryptUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 평문을 암호화
     *
     * @param password 암호화 키
     * @param plainText 암호화할 평문
     * @return 암호화된 문자열
     */
    public static String encrypt(String password, String plainText) {
        PooledPBEStringEncryptor encryptor = createEncryptor(password);
        return encryptor.encrypt(plainText);
    }

    /**
     * 암호화된 문자열을 복호화
     *
     * @param password 암호화 키
     * @param encryptedText 암호화된 문자열
     * @return 복호화된 평문
     */
    public static String decrypt(String password, String encryptedText) {
        PooledPBEStringEncryptor encryptor = createEncryptor(password);
        return encryptor.decrypt(encryptedText);
    }

    /** Encryptor 생성 (JasyptConfig와 동일한 설정) */
    private static PooledPBEStringEncryptor createEncryptor(String password) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");

        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * 테스트용 main 메서드 실행: ./gradlew :module-common:run
     * -PmainClass=app.backend.core.utils.JasyptEncryptUtil
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java JasyptEncryptUtil <secret-key> <plain-text>");
            System.out.println("Example: java JasyptEncryptUtil mySecretKey postgres");
            return;
        }

        String secretKey = args[0];
        String plainText = args[1];

        System.out.println("=================================");
        System.out.println("Jasypt Encryption Utility");
        System.out.println("=================================");
        System.out.println("Secret Key: " + secretKey);
        System.out.println("Plain Text: " + plainText);
        System.out.println("---------------------------------");

        String encrypted = encrypt(secretKey, plainText);
        System.out.println("Encrypted: ENC(" + encrypted + ")");

        String decrypted = decrypt(secretKey, encrypted);
        System.out.println("Decrypted: " + decrypted);
        System.out.println("=================================");
        System.out.println("\nAdd this to your application.yml:");
        System.out.println("  password: ENC(" + encrypted + ")");
        System.out.println("\nRun with:");
        System.out.println("  --jasypt.encryptor.password=" + secretKey);
        System.out.println("  or set environment variable:");
        System.out.println("  export JASYPT_ENCRYPTOR_PASSWORD=" + secretKey);
    }
}
