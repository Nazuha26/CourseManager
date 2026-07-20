package com.coursemanagerfx.logic.utilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleInstanceGuardTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void preventsSecondInstanceAndReleasesLockOnClose() throws Exception {
        Path lockFile = temporaryDirectory.resolve("coursemanagerfx.lock");

        try (SingleInstanceGuard first = SingleInstanceGuard.tryAcquire(lockFile)
                .orElseThrow()) {
            Optional<SingleInstanceGuard> second =
                    SingleInstanceGuard.tryAcquire(lockFile);
            assertTrue(second.isEmpty());
        }

        try (SingleInstanceGuard afterClose = SingleInstanceGuard.tryAcquire(lockFile)
                .orElseThrow()) {
            assertTrue(lockFile.toFile().isFile());
        }
    }
}
