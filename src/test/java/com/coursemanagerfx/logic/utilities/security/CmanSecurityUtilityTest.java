package com.coursemanagerfx.logic.utilities.security;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.utilities.security.exceptions.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CmanSecurityUtilityTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsAndReadsVersionOneFile() throws Exception {
        Path file = temporaryDirectory.resolve("course.cman");
        char[] seedPhrase = phrase();
        try {
            CmanSecurityUtility.createSecureFile(
                    new Group[] {new Group()},
                    file.toFile(),
                    seedPhrase);

            byte[] encrypted = Files.readAllBytes(file);
            assertArrayEquals(
                    "CMAN".getBytes(StandardCharsets.US_ASCII),
                    Arrays.copyOf(encrypted, 4));
            assertEquals(CmanCrypto.VERSION, Byte.toUnsignedInt(encrypted[4]));

            Group[] restored = CmanSecurityUtility.readSecureFile(
                    file.toFile(),
                    seedPhrase);
            assertEquals(1, restored.length);
            assertTrue(restored[0].getStudents().isEmpty());
        } finally {
            Arrays.fill(seedPhrase, '\0');
        }
    }

    @Test
    void rejectsWrongPhraseAndAuthenticatedHeaderTampering() throws Exception {
        Path file = temporaryDirectory.resolve("protected.cman");
        char[] correctPhrase = phrase();
        char[] wrongPhrase = "abacus abdomen abdominal abide ability".toCharArray();
        try {
            CmanSecurityUtility.createSecureFile(
                    new Group[] {new Group()},
                    file.toFile(),
                    correctPhrase);

            assertThrows(
                    AuthenticationException.class,
                    () -> CmanSecurityUtility.readSecureFile(
                            file.toFile(),
                            wrongPhrase));

            byte[] tampered = Files.readAllBytes(file);
            tampered[28] ^= 0x01;
            Files.write(file, tampered);
            assertThrows(
                    AuthenticationException.class,
                    () -> CmanSecurityUtility.readSecureFile(
                            file.toFile(),
                            correctPhrase));
        } finally {
            Arrays.fill(correctPhrase, '\0');
            Arrays.fill(wrongPhrase, '\0');
        }
    }

    @Test
    void updateRotatesSaltAndNonceAndKeepsBackup() throws Exception {
        Path file = temporaryDirectory.resolve("updated.cman");
        Path backup = temporaryDirectory.resolve("updated.cman.bak");
        char[] seedPhrase = phrase();
        try {
            Group[] groups = {new Group()};
            CmanSecurityUtility.createSecureFile(groups, file.toFile(), seedPhrase);
            byte[] original = Files.readAllBytes(file);

            CmanSecurityUtility.updateSecureFile(groups, file.toFile(), seedPhrase);
            byte[] updated = Files.readAllBytes(file);

            assertArrayEquals(original, Files.readAllBytes(backup));
            assertFalse(Arrays.equals(
                    Arrays.copyOfRange(original, 28, CmanCrypto.HEADER_LENGTH),
                    Arrays.copyOfRange(updated, 28, CmanCrypto.HEADER_LENGTH)));
            assertEquals(1, CmanSecurityUtility.readSecureFile(
                    file.toFile(),
                    seedPhrase).length);
        } finally {
            Arrays.fill(seedPhrase, '\0');
        }
    }

    @Test
    void refusesToOverwriteExistingCourseAndRejectsLegacyEnvelope()
            throws Exception {

        Path file = temporaryDirectory.resolve("existing.cman");
        char[] seedPhrase = phrase();
        try {
            Group[] groups = {new Group()};
            CmanSecurityUtility.createSecureFile(groups, file.toFile(), seedPhrase);
            assertThrows(
                    FileAlreadyExistsException.class,
                    () -> CmanSecurityUtility.createSecureFile(
                            groups,
                            file.toFile(),
                            seedPhrase));

            Path legacy = temporaryDirectory.resolve("legacy.cman");
            Files.write(legacy, new byte[CmanCrypto.MIN_FILE_LENGTH]);
            assertThrows(
                    AuthenticationException.class,
                    () -> CmanSecurityUtility.readSecureFile(
                            legacy.toFile(),
                            seedPhrase));
        } finally {
            Arrays.fill(seedPhrase, '\0');
        }
    }

    private static char[] phrase() {
        return "abacus abdomen abdominal abide abiding".toCharArray();
    }
}
