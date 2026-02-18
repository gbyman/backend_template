package app.backend.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EncryptionUtils} 단위 테스트
 *
 * <p>암호화/복호화, Base64 인코딩/디코딩, SHA-256 해싱 기능을 검증합니다.
 */
class EncryptionUtilsTest {

    @Nested
    @DisplayName("AES-256 암호화/복호화 테스트")
    class AES256Test {

        @Test
        @DisplayName("평문을 암호화하고 복호화하면 원본과 동일해야 함")
        void encryptAndDecrypt_shouldReturnOriginalText() {
            // Given
            String plainText = "Hello, World!";
            String secretKey = "MySecretKey123456789012345678901"; // 32 bytes

            // When
            String encrypted = EncryptionUtils.encryptAES256(plainText, secretKey);
            String decrypted = EncryptionUtils.decryptAES256(encrypted, secretKey);

            // Then
            assertThat(decrypted).isEqualTo(plainText);
        }

        @Test
        @DisplayName("같은 평문을 암호화해도 매번 다른 결과 (랜덤 IV)")
        void encrypt_shouldProduceDifferentResults() {
            // Given
            String plainText = "Hello, World!";
            String secretKey = "MySecretKey123456789012345678901";

            // When
            String encrypted1 = EncryptionUtils.encryptAES256(plainText, secretKey);
            String encrypted2 = EncryptionUtils.encryptAES256(plainText, secretKey);

            // Then
            assertThat(encrypted1).isNotEqualTo(encrypted2); // 랜덤 IV로 인해 다름
        }

        @Test
        @DisplayName("한글 텍스트 암호화/복호화")
        void encryptAndDecrypt_koreanText() {
            // Given
            String plainText = "안녕하세요, 세계!";
            String secretKey = "MySecretKey123456789012345678901";

            // When
            String encrypted = EncryptionUtils.encryptAES256(plainText, secretKey);
            String decrypted = EncryptionUtils.decryptAES256(encrypted, secretKey);

            // Then
            assertThat(decrypted).isEqualTo(plainText);
        }

        @Test
        @DisplayName("특수문자 포함 텍스트 암호화/복호화")
        void encryptAndDecrypt_specialCharacters() {
            // Given
            String plainText = "!@#$%^&*()_+-={}[]|:;<>?,./~`";
            String secretKey = "MySecretKey123456789012345678901";

            // When
            String encrypted = EncryptionUtils.encryptAES256(plainText, secretKey);
            String decrypted = EncryptionUtils.decryptAES256(encrypted, secretKey);

            // Then
            assertThat(decrypted).isEqualTo(plainText);
        }

        @Test
        @DisplayName("32바이트가 아닌 키로 암호화 (자동 해싱)")
        void encryptAndDecrypt_withShortKey() {
            // Given
            String plainText = "Hello, World!";
            String shortKey = "short"; // 32바이트 미만

            // When
            String encrypted = EncryptionUtils.encryptAES256(plainText, shortKey);
            String decrypted = EncryptionUtils.decryptAES256(encrypted, shortKey);

            // Then
            assertThat(decrypted).isEqualTo(plainText);
        }

        @Test
        @DisplayName("잘못된 암호문 복호화 시 예외 발생")
        void decrypt_invalidCipherText_shouldThrowException() {
            // Given
            String invalidCipherText = "invalid-cipher-text";
            String secretKey = "MySecretKey123456789012345678901";

            // When & Then
            assertThatThrownBy(() -> EncryptionUtils.decryptAES256(invalidCipherText, secretKey))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("AES-256 decryption failed");
        }

        @Test
        @DisplayName("다른 키로 복호화 시 예외 발생")
        void decrypt_withDifferentKey_shouldThrowException() {
            // Given
            String plainText = "Hello, World!";
            String secretKey1 = "Key1234567890123456789012345678";
            String secretKey2 = "Key2234567890123456789012345678"; // 다른 키

            String encrypted = EncryptionUtils.encryptAES256(plainText, secretKey1);

            // When & Then
            assertThatThrownBy(() -> EncryptionUtils.decryptAES256(encrypted, secretKey2))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("AES-256 decryption failed");
        }
    }

    @Nested
    @DisplayName("Base64 인코딩/디코딩 테스트")
    class Base64Test {

        @Test
        @DisplayName("문자열 Base64 인코딩/디코딩")
        void encodeAndDecode_string() {
            // Given
            String text = "Hello, World!";

            // When
            String encoded = EncryptionUtils.encodeBase64(text);
            String decoded = EncryptionUtils.decodeBase64ToString(encoded);

            // Then
            assertThat(decoded).isEqualTo(text);
        }

        @Test
        @DisplayName("바이트 배열 Base64 인코딩/디코딩")
        void encodeAndDecode_bytes() {
            // Given
            byte[] data = "Hello, World!".getBytes();

            // When
            String encoded = EncryptionUtils.encodeBase64(data);
            byte[] decoded = EncryptionUtils.decodeBase64(encoded);

            // Then
            assertThat(decoded).isEqualTo(data);
        }

        @Test
        @DisplayName("빈 문자열 인코딩")
        void encode_emptyString() {
            // When
            String encoded = EncryptionUtils.encodeBase64("");

            // Then
            assertThat(encoded).isEmpty();
        }

        @Test
        @DisplayName("null 문자열 인코딩")
        void encode_nullString() {
            // When
            String encoded = EncryptionUtils.encodeBase64((String) null);

            // Then
            assertThat(encoded).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열 디코딩")
        void decode_emptyString() {
            // When
            String decoded = EncryptionUtils.decodeBase64ToString("");

            // Then
            assertThat(decoded).isEmpty();
        }
    }

    @Nested
    @DisplayName("SHA-256 해싱 테스트")
    class SHA256Test {

        @Test
        @DisplayName("같은 텍스트는 항상 같은 해시 생성")
        void hash_sameTextProducesSameHash() {
            // Given
            String text = "Hello, World!";

            // When
            String hash1 = EncryptionUtils.hashSHA256(text);
            String hash2 = EncryptionUtils.hashSHA256(text);

            // Then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("다른 텍스트는 다른 해시 생성")
        void hash_differentTextProducesDifferentHash() {
            // Given
            String text1 = "Hello, World!";
            String text2 = "Hello, World";

            // When
            String hash1 = EncryptionUtils.hashSHA256(text1);
            String hash2 = EncryptionUtils.hashSHA256(text2);

            // Then
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("SHA-256 해시 검증 성공")
        void verifySHA256_shouldReturnTrue() {
            // Given
            String text = "Hello, World!";
            String hash = EncryptionUtils.hashSHA256(text);

            // When
            boolean verified = EncryptionUtils.verifySHA256(text, hash);

            // Then
            assertThat(verified).isTrue();
        }

        @Test
        @DisplayName("SHA-256 해시 검증 실패")
        void verifySHA256_shouldReturnFalse() {
            // Given
            String text = "Hello, World!";
            String wrongHash = "wrong-hash";

            // When
            boolean verified = EncryptionUtils.verifySHA256(text, wrongHash);

            // Then
            assertThat(verified).isFalse();
        }

        @Test
        @DisplayName("SHA-256 Base64 인코딩")
        void hashSHA256ToBase64() {
            // Given
            String text = "Hello, World!";

            // When
            String hash = EncryptionUtils.hashSHA256ToBase64(text);

            // Then
            assertThat(hash).isNotEmpty();
            assertThat(hash).isBase64(); // Base64 형식인지 확인
        }
    }

    @Nested
    @DisplayName("비밀번호 해싱 테스트")
    class PasswordHashingTest {

        @Test
        @DisplayName("비밀번호 해싱 및 검증 성공")
        void hashAndVerifyPassword_shouldReturnTrue() {
            // Given
            String password = "myPassword123!";
            String salt = EncryptionUtils.generateSalt(16);

            // When
            String hashedPassword = EncryptionUtils.hashPasswordWithSalt(password, salt);
            boolean verified = EncryptionUtils.verifyPassword(password, hashedPassword, salt);

            // Then
            assertThat(verified).isTrue();
        }

        @Test
        @DisplayName("잘못된 비밀번호 검증 실패")
        void verifyPassword_wrongPassword_shouldReturnFalse() {
            // Given
            String password = "myPassword123!";
            String wrongPassword = "wrongPassword";
            String salt = EncryptionUtils.generateSalt(16);
            String hashedPassword = EncryptionUtils.hashPasswordWithSalt(password, salt);

            // When
            boolean verified = EncryptionUtils.verifyPassword(wrongPassword, hashedPassword, salt);

            // Then
            assertThat(verified).isFalse();
        }

        @Test
        @DisplayName("다른 Salt로 검증 실패")
        void verifyPassword_differentSalt_shouldReturnFalse() {
            // Given
            String password = "myPassword123!";
            String salt1 = EncryptionUtils.generateSalt(16);
            String salt2 = EncryptionUtils.generateSalt(16);
            String hashedPassword = EncryptionUtils.hashPasswordWithSalt(password, salt1);

            // When
            boolean verified = EncryptionUtils.verifyPassword(password, hashedPassword, salt2);

            // Then
            assertThat(verified).isFalse();
        }
    }

    @Nested
    @DisplayName("랜덤 키/Salt 생성 테스트")
    class RandomGenerationTest {

        @Test
        @DisplayName("랜덤 AES 키 생성")
        void generateRandomKey() {
            // When
            String key = EncryptionUtils.generateRandomKey();

            // Then
            assertThat(key).isNotEmpty();
            assertThat(key).isBase64();
        }

        @Test
        @DisplayName("랜덤 Salt 생성")
        void generateSalt() {
            // When
            String salt = EncryptionUtils.generateSalt(16);

            // Then
            assertThat(salt).isNotEmpty();
            assertThat(salt).isBase64();
        }

        @Test
        @DisplayName("매번 다른 랜덤 키 생성")
        void generateRandomKey_shouldProduceDifferentKeys() {
            // When
            String key1 = EncryptionUtils.generateRandomKey();
            String key2 = EncryptionUtils.generateRandomKey();

            // Then
            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("생성된 랜덤 키로 암호화/복호화 성공")
        void useGeneratedKey_shouldWork() {
            // Given
            String plainText = "Hello, World!";
            String generatedKey = EncryptionUtils.generateRandomKey();

            // When
            String encrypted = EncryptionUtils.encryptAES256(plainText, generatedKey);
            String decrypted = EncryptionUtils.decryptAES256(encrypted, generatedKey);

            // Then
            assertThat(decrypted).isEqualTo(plainText);
        }
    }
}
