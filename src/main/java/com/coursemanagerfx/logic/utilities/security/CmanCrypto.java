package com.coursemanagerfx.logic.utilities.security;

import com.coursemanagerfx.logic.utilities.security.exceptions.AuthenticationException;
import com.coursemanagerfx.logic.utilities.security.exceptions.DecryptionException;
import com.coursemanagerfx.logic.utilities.security.exceptions.EncryptionException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/** Cryptographic envelope for the versioned CMAN file format. */
final class CmanCrypto {

    static final int VERSION = 1;
    static final int HEADER_LENGTH = 56;
    static final int MIN_FILE_LENGTH = HEADER_LENGTH + 16;

    private static final byte[] MAGIC = {'C', 'M', 'A', 'N'};
    private static final int KDF_ARGON2ID = 1;
    private static final int CIPHER_CHACHA20_POLY1305 = 1;

    private static final int MEMORY_KIB = 64 * 1024;
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH = 16;

    private static final int MAX_MEMORY_KIB = 256 * 1024;
    private static final int MAX_ITERATIONS = 10;
    private static final int MAX_PARALLELISM = 4;
    private static final int MAX_CIPHERTEXT_LENGTH = 64 * 1024 * 1024;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CmanCrypto() {
    }

    static byte[] encrypt(byte[] plaintext, char[] seedPhrase)
            throws EncryptionException {

        Objects.requireNonNull(plaintext, "plaintext");
        requireSeedPhrase(seedPhrase);
        if (plaintext.length > MAX_CIPHERTEXT_LENGTH - TAG_LENGTH) {
            throw new IllegalArgumentException("CMAN payload is too large");
        }

        byte[] salt = randomBytes(SALT_LENGTH);
        byte[] nonce = randomBytes(NONCE_LENGTH);
        byte[] key = null;
        byte[] header = null;

        try {
            int ciphertextLength = Math.addExact(plaintext.length, TAG_LENGTH);
            header = buildHeader(salt, nonce, ciphertextLength);
            key = deriveKey(seedPhrase, salt, MEMORY_KIB, ITERATIONS, PARALLELISM);

            Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key, "ChaCha20"),
                    new IvParameterSpec(nonce));
            cipher.updateAAD(header);
            byte[] ciphertext = cipher.doFinal(plaintext);

            return ByteBuffer.allocate(HEADER_LENGTH + ciphertext.length)
                    .put(header)
                    .put(ciphertext)
                    .array();
        } catch (GeneralSecurityException | ArithmeticException exception) {
            throw new EncryptionException("Could not encrypt the CMAN file", exception);
        } finally {
            clear(key);
            clear(header);
            clear(salt);
            clear(nonce);
        }
    }

    static byte[] decrypt(byte[] encryptedFile, char[] seedPhrase)
            throws DecryptionException {

        Objects.requireNonNull(encryptedFile, "encryptedFile");
        requireSeedPhrase(seedPhrase);

        Header header = parseHeader(encryptedFile);
        byte[] aad = Arrays.copyOf(encryptedFile, HEADER_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(
                encryptedFile,
                HEADER_LENGTH,
                encryptedFile.length);
        byte[] key = null;

        try {
            key = deriveKey(
                    seedPhrase,
                    header.salt(),
                    header.memoryKiB(),
                    header.iterations(),
                    header.parallelism());

            Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key, "ChaCha20"),
                    new IvParameterSpec(header.nonce()));
            cipher.updateAAD(aad);
            return cipher.doFinal(ciphertext);
        } catch (AEADBadTagException exception) {
            throw new AuthenticationException(
                    "Invalid seed phrase or corrupted CMAN file",
                    exception);
        } catch (GeneralSecurityException exception) {
            throw new DecryptionException("Could not decrypt the CMAN file", exception);
        } finally {
            clear(key);
            clear(aad);
            clear(ciphertext);
            header.clear();
        }
    }

    private static Header parseHeader(byte[] encryptedFile) {
        if (encryptedFile.length < MIN_FILE_LENGTH) {
            throw new AuthenticationException("CMAN file is too short or corrupted");
        }

        ByteBuffer buffer = ByteBuffer.wrap(encryptedFile, 0, HEADER_LENGTH)
                .order(ByteOrder.BIG_ENDIAN);
        byte[] magic = new byte[MAGIC.length];
        buffer.get(magic);
        if (!Arrays.equals(MAGIC, magic)) {
            throw new AuthenticationException("Invalid CMAN magic");
        }

        int version = Byte.toUnsignedInt(buffer.get());
        int kdf = Byte.toUnsignedInt(buffer.get());
        int cipher = Byte.toUnsignedInt(buffer.get());
        int flags = Byte.toUnsignedInt(buffer.get());
        int memoryKiB = buffer.getInt();
        int iterations = buffer.getInt();
        int parallelism = buffer.getInt();
        int saltLength = Byte.toUnsignedInt(buffer.get());
        int nonceLength = Byte.toUnsignedInt(buffer.get());
        int tagLength = Byte.toUnsignedInt(buffer.get());
        int reserved = Byte.toUnsignedInt(buffer.get());
        int ciphertextLength = buffer.getInt();

        if (version != VERSION) {
            throw new AuthenticationException("Unsupported CMAN version: " + version);
        }
        if (kdf != KDF_ARGON2ID || cipher != CIPHER_CHACHA20_POLY1305) {
            throw new AuthenticationException("Unsupported CMAN cryptographic profile");
        }
        if (flags != 0 || reserved != 0) {
            throw new AuthenticationException("Unsupported CMAN header flags");
        }
        if (memoryKiB < 8 * 1024 || memoryKiB > MAX_MEMORY_KIB
                || iterations < 1 || iterations > MAX_ITERATIONS
                || parallelism < 1 || parallelism > MAX_PARALLELISM) {
            throw new AuthenticationException("Unsafe or invalid Argon2id parameters");
        }
        if (saltLength != SALT_LENGTH
                || nonceLength != NONCE_LENGTH
                || tagLength != TAG_LENGTH) {
            throw new AuthenticationException("Invalid CMAN salt, nonce or tag length");
        }
        if (ciphertextLength < TAG_LENGTH
                || ciphertextLength > MAX_CIPHERTEXT_LENGTH
                || encryptedFile.length != HEADER_LENGTH + ciphertextLength) {
            throw new AuthenticationException("Invalid CMAN payload length");
        }

        byte[] salt = new byte[SALT_LENGTH];
        byte[] nonce = new byte[NONCE_LENGTH];
        buffer.get(salt);
        buffer.get(nonce);
        return new Header(memoryKiB, iterations, parallelism, salt, nonce);
    }

    private static byte[] buildHeader(
            byte[] salt,
            byte[] nonce,
            int ciphertextLength) {

        return ByteBuffer.allocate(HEADER_LENGTH)
                .order(ByteOrder.BIG_ENDIAN)
                .put(MAGIC)
                .put((byte) VERSION)
                .put((byte) KDF_ARGON2ID)
                .put((byte) CIPHER_CHACHA20_POLY1305)
                .put((byte) 0)
                .putInt(MEMORY_KIB)
                .putInt(ITERATIONS)
                .putInt(PARALLELISM)
                .put((byte) SALT_LENGTH)
                .put((byte) NONCE_LENGTH)
                .put((byte) TAG_LENGTH)
                .put((byte) 0)
                .putInt(ciphertextLength)
                .put(salt)
                .put(nonce)
                .array();
    }

    private static byte[] deriveKey(
            char[] seedPhrase,
            byte[] salt,
            int memoryKiB,
            int iterations,
            int parallelism) {

        Argon2Parameters parameters = new Argon2Parameters.Builder(
                Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(memoryKiB)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);
        byte[] key = new byte[KEY_LENGTH];
        boolean generated = false;
        try {
            generator.generateBytes(seedPhrase, key);
            generated = true;
            return key;
        } finally {
            parameters.clear();
            if (!generated) clear(key);
        }
    }

    private static void requireSeedPhrase(char[] seedPhrase) {
        if (seedPhrase == null || seedPhrase.length == 0) {
            throw new IllegalArgumentException("Seed phrase must not be empty");
        }
    }

    private static byte[] randomBytes(int length) {
        byte[] result = new byte[length];
        SECURE_RANDOM.nextBytes(result);
        return result;
    }

    private static void clear(byte[] bytes) {
        if (bytes != null) Arrays.fill(bytes, (byte) 0);
    }

    private record Header(
            int memoryKiB,
            int iterations,
            int parallelism,
            byte[] salt,
            byte[] nonce) {

        private void clear() {
            CmanCrypto.clear(salt);
            CmanCrypto.clear(nonce);
        }
    }
}
