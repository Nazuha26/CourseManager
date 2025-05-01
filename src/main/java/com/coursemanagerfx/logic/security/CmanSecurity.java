package com.coursemanagerfx.logic.security;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CmanSecurity {

    private static final int PASSWORD_LENGTH = 10;

    private static final int KEY_LEN  = 256;                // бит
    private static final int ITER    = 100_000;             // PBKDF2-итерации
    private static final String P_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "0123456789" +
                    "!@#$%&*_+=|;:\"<>?";

    public static String generatePassword() {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(P_CHARS.charAt(rnd.nextInt(P_CHARS.length())));
        }
        return sb.toString();
    }

    /** Метод для генерации соли и IV */
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static byte[] generateIV() {
        byte[] iv = new byte[12]; // стандарт для GCM
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /** Генерирует устойчивый PBKDF2-хэш пароля + соль и кодирует всё в Base64 */
    public static String hashPassword(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITER, KEY_LEN);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    public static byte[] veracryptStyleHash(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITER, KEY_LEN);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        return factory.generateSecret(spec).getEncoded();
    }

    public static boolean verifyPassword(String inputPassword, byte[] salt, byte[] expectedHash) throws Exception {
        byte[] inputHash = veracryptStyleHash(inputPassword, salt);
        return MessageDigest.isEqual(inputHash, expectedHash);
    }

    // PBKDF2 → AES-256 key
    public static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITER, KEY_LEN);
        byte[] keyBytes = f.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}
