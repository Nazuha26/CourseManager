/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.utilities.security;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.utilities.security.exceptions.AuthenticationException;
import com.coursemanagerfx.logic.utilities.security.exceptions.DecryptionException;
import com.coursemanagerfx.logic.utilities.security.exceptions.EncryptionException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

import static com.coursemanagerfx.logic.utilities.security.BinaryPlainCmanUtility.buildPlainCman;

/** Reads and atomically writes encrypted CMAN v1 course files. */
public final class CmanSecurityUtility {

    private static final long MAX_FILE_SIZE =
            CmanCrypto.HEADER_LENGTH + 64L * 1024L * 1024L;

    private CmanSecurityUtility() {
    }

    public static void createSecureFile(
            Group[] groups,
            File outFile,
            char[] seedPhrase) throws IOException, EncryptionException {

        Objects.requireNonNull(outFile, "outFile");
        Path target = outFile.toPath().toAbsolutePath().normalize();
        if (Files.exists(target)) {
            throw new FileAlreadyExistsException(target.toString());
        }

        byte[] plaintext = buildPlainCman(Objects.requireNonNull(groups, "groups"));
        byte[] encrypted = null;
        try {
            encrypted = CmanCrypto.encrypt(plaintext, seedPhrase);
            writeNewAtomically(target, encrypted);
        } finally {
            clear(plaintext);
            clear(encrypted);
        }
    }

    public static void updateSecureFile(
            Group[] groups,
            File file,
            char[] seedPhrase) throws IOException, EncryptionException {

        Objects.requireNonNull(file, "file");
        Path target = file.toPath().toAbsolutePath().normalize();
        if (!Files.isRegularFile(target)) {
            throw new NoSuchFileException(target.toString());
        }

        byte[] plaintext = buildPlainCman(Objects.requireNonNull(groups, "groups"));
        byte[] encrypted = null;
        try {
            encrypted = CmanCrypto.encrypt(plaintext, seedPhrase);
            replaceAtomicallyWithBackup(target, encrypted);
        } finally {
            clear(plaintext);
            clear(encrypted);
        }
    }

    public static Group[] readSecureFile(File file, char[] seedPhrase)
            throws IOException {

        Objects.requireNonNull(file, "file");
        Path source = file.toPath().toAbsolutePath().normalize();
        long size = Files.size(source);
        if (size < CmanCrypto.MIN_FILE_LENGTH || size > MAX_FILE_SIZE) {
            throw new AuthenticationException("Invalid CMAN file size");
        }

        byte[] encrypted = Files.readAllBytes(source);
        byte[] plaintext = null;
        try {
            plaintext = CmanCrypto.decrypt(encrypted, seedPhrase);
            try {
                return BinaryPlainCmanUtility.parsePlainCman(
                        new ByteArrayInputStream(plaintext));
            } catch (IOException | RuntimeException exception) {
                throw new DecryptionException(
                        "The encrypted CMAN payload is corrupted",
                        exception);
            }
        } finally {
            clear(encrypted);
            clear(plaintext);
        }
    }

    private static void writeNewAtomically(Path target, byte[] bytes)
            throws IOException {

        Path parent = requireParent(target);
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(
                parent,
                "." + target.getFileName() + ".",
                ".tmp");

        try {
            writeAndForce(temporary, bytes);
            if (Files.exists(target)) {
                throw new FileAlreadyExistsException(target.toString());
            }
            moveAtomically(temporary, target, false);
        } finally {
            deleteTemporary(temporary);
        }
    }

    private static void replaceAtomicallyWithBackup(Path target, byte[] bytes)
            throws IOException {

        Path parent = requireParent(target);
        Path temporary = Files.createTempFile(
                parent,
                "." + target.getFileName() + ".",
                ".tmp");

        try {
            writeAndForce(temporary, bytes);
            createBackup(target);
            moveAtomically(temporary, target, true);
        } finally {
            deleteTemporary(temporary);
        }
    }

    private static void createBackup(Path target) throws IOException {
        Path parent = requireParent(target);
        Path backup = target.resolveSibling(target.getFileName() + ".bak");
        Path temporaryBackup = Files.createTempFile(
                parent,
                "." + target.getFileName() + ".bak.",
                ".tmp");

        try {
            Files.copy(target, temporaryBackup, StandardCopyOption.REPLACE_EXISTING);
            forceFile(temporaryBackup);
            moveAtomically(temporaryBackup, backup, true);
        } finally {
            deleteTemporary(temporaryBackup);
        }
    }

    private static void writeAndForce(Path file, byte[] bytes) throws IOException {
        try (FileChannel channel = FileChannel.open(
                file,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) channel.write(buffer);
            channel.force(true);
        }
    }

    private static void forceFile(Path file) throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    private static void moveAtomically(
            Path source,
            Path target,
            boolean replace) throws IOException {

        try {
            if (replace) {
                Files.move(
                        source,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (AtomicMoveNotSupportedException exception) {
            throw new IOException(
                    "Atomic CMAN save is not supported by this filesystem",
                    exception);
        }
    }

    private static Path requireParent(Path target) throws IOException {
        Path parent = target.getParent();
        if (parent == null) {
            throw new IOException("CMAN file must have a parent directory");
        }
        return parent;
    }

    private static void deleteTemporary(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            file.toFile().deleteOnExit();
        }
    }

    private static void clear(byte[] bytes) {
        if (bytes != null) Arrays.fill(bytes, (byte) 0);
    }
}
