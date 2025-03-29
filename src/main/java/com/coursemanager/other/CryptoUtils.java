package com.coursemanager.other;

import com.coursemanager.CM_HELPER;
import io.github.cdimascio.dotenv.Dotenv;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final byte[] KEY;

    static {
        Dotenv dotenv = Dotenv.configure()
                .directory(CM_HELPER.SECRET_KEY_DIR.toString())
                .filename(".env")
                .load();
        String keyString = dotenv.get("CMAN_SECRET_KEY");
        if (keyString == null || keyString.length() != 16) {
            throw new RuntimeException("Неверный или отсутствующий ключ в .env");
        }
        KEY = keyString.getBytes();
    }

    public static byte[] encrypt(byte[] data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] encryptedData) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(encryptedData);
    }
}