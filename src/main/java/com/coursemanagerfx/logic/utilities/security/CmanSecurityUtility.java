/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.utilities.security;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.utilities.security.exceptions.EncryptionException;
import com.coursemanagerfx.logic.utilities.security.exceptions.AuthenticationException;
import com.coursemanagerfx.logic.utilities.security.exceptions.DecryptionException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;

import static com.coursemanagerfx.logic.utilities.security.BinaryPlainCmanUtility.buildPlainCman;

/*
 * Класс для безопасного шифрования и обновления бинарных *.cman‑файлов, содержащих данные Group[].
 *
 * Логика шифрования:
 * – Генерируется случайная соль (16 байт) и записывается в начале файла.
 * – Из пользовательского 10-значного пароля по алгоритму PBKDF2 (HMAC-SHA256, 200 000 итераций) создаётся AES‑ключ (256 бит).
 * – Генерируется случайный IV (инициализационный вектор, 16 байт) для режима AES/CBC.
 * – Исходные данные (buildPlainCman()) шифруются с помощью AES/CBC/PKCS5Padding.
 * – Для проверки целостности и подлинности рассчитывается HMAC‑SHA256 от (IV + зашифрованные данные).
 * – Формат файла: [SALT (16 байт)] [IV (16 байт)] [ЗАШИФРОВАННЫЕ ДАННЫЕ] [HMAC (32 байта)].
 *
 * Создание файла: метод createSecureFile(...) — полностью записывает все блоки заново.
 * Обновление файла: метод updateSecureFile(...) — использует уже сохранённую соль, пересоздаёт IV и HMAC, перезаписывает содержимое.
 */

public class CmanSecurityUtility {

    /**
     * Creates a new encrypted file with student group data.
     * It uses a random salt and IV, encrypts the data with AES,
     * and adds HMAC to check the file later.
     *
     * @param groups   Array of groups to save.
     * @param outFile  File to write the encrypted data into.
     * @param password 10-character password used for encryption.
     * @throws Exception If encryption or file writing fails.
     */
    public static void createSecureFile(Group[] groups, File outFile, String password) throws Exception {
        byte[] plain      = buildPlainCman(groups);
        byte[] salt       = randomBytes(SALT_LEN);
        SecretKey key     = deriveKey(password, salt);
        byte[] iv         = randomBytes(IV_LEN);
        byte[] cipherData = null;
        try { cipherData = aesCbcEncrypt(plain, key, iv); }
        catch (Exception e) { throw new EncryptionException("Encryption failed", e); }
        byte[] hmac       = computeHmac(key, iv, cipherData);

        try (DataOutputStream o = new DataOutputStream(new FileOutputStream(outFile))) {
            o.write(salt);
            o.write(iv);
            o.write(cipherData);
            o.write(hmac);
        }
    }

    /**
     * Updates an existing encrypted file with new group data.
     * It keeps the old salt, but uses a new IV and HMAC.
     * This method overwrites the file safely.
     *
     * @param groups  New group data to save.
     * @param file    File to overwrite.
     * @param password Password used for encryption.
     * @throws Exception If something goes wrong during encryption or writing.
     */
    public static void updateSecureFile(Group[] groups, File file, String password) throws Exception {
        byte[] plain   = buildPlainCman(groups);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            // read salt
            byte[] salt = new byte[SALT_LEN];
            raf.readFully(salt);

            // key, IV, cipher data, HMAC
            SecretKey key     = deriveKey(password, salt);
            byte[] iv         = randomBytes(IV_LEN);
            byte[] cipherData = null;
            try { cipherData = aesCbcEncrypt(plain, key, iv); }
            catch (Exception e) { throw new EncryptionException("Encryption failed", e); }
            byte[] hmac       = computeHmac(key, iv, cipherData);

            // overwrite IV, cipher data and HMAC
            raf.seek(SALT_LEN);
            raf.write(iv);
            raf.write(cipherData);
            raf.write(hmac);

            long newLen = SALT_LEN + IV_LEN + cipherData.length + HMAC_LEN;
            raf.setLength(newLen);
        }
    }

    /**
     * Reads and decrypts a secure file with group data.
     * It checks the HMAC to make sure the file is not changed.
     * If the password is correct, it returns the data.
     *
     * @param file     Encrypted file to read.
     * @param password Password used for decryption.
     * @return Array of groups read from the file.
     * @throws Exception If the password is wrong or file is damaged.
     */
    public static Group[] readSecureFile(File file, String password) throws Exception {
        byte[] fileBytes;
        try { fileBytes = readAllBytes(file); } catch (IOException e) { throw new DecryptionException("Cannot read encrypted file.", e); }

        if (fileBytes.length < SALT_LEN + IV_LEN + HMAC_LEN)
            throw new AuthenticationException("Encrypted file is too short or corrupted.");

        byte[] salt = extract(fileBytes, 0, SALT_LEN);
        byte[] iv = extract(fileBytes, SALT_LEN, IV_LEN);
        int cipherDataLen = fileBytes.length - SALT_LEN - IV_LEN - HMAC_LEN;
        byte[] cipherData = extract(fileBytes, SALT_LEN + IV_LEN, cipherDataLen);
        byte[] storedHmac = extract(fileBytes, SALT_LEN + IV_LEN + cipherDataLen, HMAC_LEN);

        // recover the key and check the HMAC
        SecretKey key = deriveKey(password, salt);
        byte[] computedHmac = computeHmac(key, iv, cipherData);
        if (!MessageDigest.isEqual(storedHmac, computedHmac)) {
            throw new AuthenticationException("Invalid password or corrupted file.");
        }

        // decrypt
        byte[] plain = aesCbcDecrypt(cipherData, key, iv);
        return BinaryPlainCmanUtility.parsePlainCman(new ByteArrayInputStream(plain));
    }

    /* ==================== HELPER METHODS ==================== */
    private static final int PASSWORD_LENGTH = 10;
    private static final String P_CHARS =
            "ABCDEFGHJKMNPQRSTUVWXYZ" +
                    "abcdefghjkmnpqrstuvwxyz" +
                    "0123456789" +
                    "!@#$_<>?-*&";

    /**
     * Generates a random 10-character password.
     *
     * @return A new strong password.
     */
    public static String generatePassword() {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(P_CHARS.charAt(rnd.nextInt(P_CHARS.length())));
        }
        return sb.toString();
    }

    private static final int SALT_LEN = 16;
    private static final int IV_LEN   = 16;
    private static final int HMAC_LEN = 32;
    private static final int ITER     = 200_000;

    private static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        new SecureRandom().nextBytes(b);
        return b;
    }

    private static SecretKey deriveKey(String pwd, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, ITER, 256);
        SecretKey tmp   = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private static byte[] aesCbcEncrypt(byte[] data, SecretKey key, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return c.doFinal(data);
    }

    private static byte[] aesCbcDecrypt(byte[] cipherData, SecretKey key, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return c.doFinal(cipherData);
    }

    private static byte[] computeHmac(SecretKey key, byte[] iv, byte[] cipherData) throws Exception {
        Mac m = Mac.getInstance("HmacSHA256");
        m.init(new SecretKeySpec(key.getEncoded(), "HmacSHA256"));
        m.update(iv);
        return m.doFinal(cipherData);
    }

    private static byte[] extract(byte[] source, int offset, int len) {
        byte[] part = new byte[len];
        System.arraycopy(source, offset, part, 0, len);
        return part;
    }

    private static byte[] readAllBytes(File file) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             InputStream in = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toByteArray();
        }
    }
}
