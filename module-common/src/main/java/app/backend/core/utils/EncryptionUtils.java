package app.backend.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.experimental.UtilityClass;

/**
 * Encryption and Hashing Utility Provides AES-256 encryption/decryption, Base64 encoding, and
 * SHA-256 hashing.
 */
@UtilityClass
public class EncryptionUtils {

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES = "AES";
    private static final int IV_SIZE = 16; // 128 bits
    private static final int AES_KEY_SIZE = 32; // 256 bits
    private static final int HEX_BYTE_MASK = 0xff;

    /**
     * Encrypt text using AES-256-CBC
     *
     * @param plainText Text to encrypt
     * @param secretKey Secret key (must be 32 bytes for AES-256)
     * @return Base64 encoded encrypted text (IV + encrypted data)
     */
    public String encryptAES256(String plainText, String secretKey) {
        try {
            // Generate random IV
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Create key spec
            SecretKeySpec keySpec = createKeySpec(secretKey);

            // Encrypt
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV + encrypted data
            byte[] combined = new byte[IV_SIZE + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_SIZE);
            System.arraycopy(encrypted, 0, combined, IV_SIZE, encrypted.length);

            // Encode to Base64
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("AES-256 encryption failed", e);
        }
    }

    /**
     * Decrypt AES-256-CBC encrypted text
     *
     * @param encryptedText Base64 encoded encrypted text (IV + encrypted data)
     * @param secretKey Secret key (must be 32 bytes for AES-256)
     * @return Decrypted plain text
     */
    public String decryptAES256(String encryptedText, String secretKey) {
        try {
            // Decode from Base64
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Extract IV
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, IV_SIZE);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Extract encrypted data
            byte[] encrypted = new byte[combined.length - IV_SIZE];
            System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);

            // Create key spec
            SecretKeySpec keySpec = createKeySpec(secretKey);

            // Decrypt
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("AES-256 decryption failed", e);
        }
    }

    /** Create AES key spec from string If key is not 32 bytes, it will be hashed using SHA-256 */
    private SecretKeySpec createKeySpec(String secretKey) {
        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

            // If key is not 32 bytes (256 bits), hash it with SHA-256
            if (keyBytes.length != AES_KEY_SIZE) {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                keyBytes = sha.digest(keyBytes);
            }

            return new SecretKeySpec(keyBytes, AES);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create key spec", e);
        }
    }

    /**
     * Encode bytes to Base64 string
     *
     * @param data Bytes to encode
     * @return Base64 encoded string
     */
    public String encodeBase64(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Encode string to Base64
     *
     * @param text Text to encode
     * @return Base64 encoded string
     */
    public String encodeBase64(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode Base64 string to bytes
     *
     * @param encoded Base64 encoded string
     * @return Decoded bytes
     */
    public byte[] decodeBase64(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(encoded);
    }

    /**
     * Decode Base64 string to text
     *
     * @param encoded Base64 encoded string
     * @return Decoded text
     */
    public String decodeBase64ToString(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * Hash text using SHA-256
     *
     * @param text Text to hash
     * @return Hex string of SHA-256 hash
     */
    public String hashSHA256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);

        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }

    /**
     * Hash text using SHA-256 (returns Base64)
     *
     * @param text Text to hash
     * @return Base64 string of SHA-256 hash
     */
    public String hashSHA256ToBase64(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }

    /**
     * Verify if text matches SHA-256 hash
     *
     * @param text Text to verify
     * @param hash SHA-256 hash (hex string)
     * @return true if text matches hash
     */
    public boolean verifySHA256(String text, String hash) {
        String textHash = hashSHA256(text);
        return textHash.equalsIgnoreCase(hash);
    }

    /** Convert byte array to hex string */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(HEX_BYTE_MASK & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Generate random encryption key (32 bytes for AES-256)
     *
     * @return Base64 encoded random key
     */
    public String generateRandomKey() {
        byte[] key = new byte[AES_KEY_SIZE]; // 256 bits
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * Generate random salt for password hashing
     *
     * @param length Salt length in bytes (recommended: 16 or 32)
     * @return Base64 encoded random salt
     */
    public String generateSalt(int length) {
        byte[] salt = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hash password with salt using SHA-256
     *
     * @param password Password to hash
     * @param salt Salt (Base64 encoded)
     * @return Hex string of hashed password
     */
    public String hashPasswordWithSalt(String password, String salt) {
        String combined = password + salt;
        return hashSHA256(combined);
    }

    /**
     * Verify password against hashed password
     *
     * @param password Plain password
     * @param hashedPassword Hashed password (hex string)
     * @param salt Salt used for hashing (Base64 encoded)
     * @return true if password matches
     */
    public boolean verifyPassword(String password, String hashedPassword, String salt) {
        String computedHash = hashPasswordWithSalt(password, salt);
        return computedHash.equalsIgnoreCase(hashedPassword);
    }
}
