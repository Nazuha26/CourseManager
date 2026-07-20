package com.coursemanagerfx.logic.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;

/** Holds an operating-system file lock for the lifetime of the application. */
public final class SingleInstanceGuard implements AutoCloseable {

    private final FileChannel channel;
    private final FileLock lock;

    private SingleInstanceGuard(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * Attempts to acquire the application lock. An empty result means another
     * process (or another application instance in this JVM) owns the lock.
     */
    public static Optional<SingleInstanceGuard> tryAcquire(Path lockFile)
            throws IOException {

        Path normalized = Objects.requireNonNull(lockFile, "lockFile")
                .toAbsolutePath()
                .normalize();
        Path parent = normalized.getParent();
        if (parent == null) {
            throw new IOException("Instance lock file must have a parent directory");
        }
        Files.createDirectories(parent);

        FileChannel channel = FileChannel.open(
                normalized,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE);
        FileLock lock = null;
        try {
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException exception) {
                // The same JVM already owns the lock.
            }
            if (lock == null) {
                channel.close();
                return Optional.empty();
            }

            byte[] processId = Long.toString(ProcessHandle.current().pid())
                    .getBytes(StandardCharsets.US_ASCII);
            channel.truncate(0);
            channel.position(0);
            ByteBuffer buffer = ByteBuffer.wrap(processId);
            while (buffer.hasRemaining()) channel.write(buffer);
            channel.force(true);
            return Optional.of(new SingleInstanceGuard(channel, lock));
        } catch (IOException | RuntimeException exception) {
            if (lock != null && lock.isValid()) {
                try {
                    lock.release();
                } catch (IOException suppressed) {
                    exception.addSuppressed(suppressed);
                }
            }
            try {
                channel.close();
            } catch (IOException suppressed) {
                exception.addSuppressed(suppressed);
            }
            throw exception;
        }
    }

    @Override
    public void close() throws IOException {
        IOException failure = null;
        try {
            if (lock.isValid()) lock.release();
        } catch (IOException exception) {
            failure = exception;
        }
        try {
            channel.close();
        } catch (IOException exception) {
            if (failure == null) failure = exception;
            else failure.addSuppressed(exception);
        }
        if (failure != null) throw failure;
    }
}
