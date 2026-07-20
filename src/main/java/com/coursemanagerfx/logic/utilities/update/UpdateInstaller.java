package com.coursemanagerfx.logic.utilities.update;

import com.coursemanagerfx.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class UpdateInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateInstaller.class);

    static final String SUCCESS_ARGUMENT_PREFIX =
            "--coursemanagerfx-update-ready=";

    private static final Duration ELEVATION_TIMEOUT = Duration.ofSeconds(90);
    private static final long MAX_ARCHIVE_SIZE = 512L * 1024L * 1024L;
    private static final long MAX_SIGNATURE_SIZE = 4L * 1024L;

    private final GitHubReleaseClient releaseClient;

    UpdateInstaller(GitHubReleaseClient releaseClient) {
        this.releaseClient = Objects.requireNonNull(releaseClient, "releaseClient");
    }

    void prepareAndLaunch(
            ReleaseInfo release,
            Consumer<Double> progressCallback) throws IOException {

        Objects.requireNonNull(release, "release");
        Consumer<Double> progress = progressCallback == null
                ? ignored -> { }
                : progressCallback;

        if (!isWindows()) {
            throw new IOException("Automatic installation is supported only on Windows");
        }

        Path stagingRoot = Files.createTempDirectory("coursemanagerfx-update-");
        boolean installerStarted = false;

        try {
            Path archive = stagingRoot.resolve(release.archive().name());
            Path signature = stagingRoot.resolve(release.signature().name());

            releaseClient.download(
                    release.archive(),
                    archive,
                    MAX_ARCHIVE_SIZE,
                    value -> progress.accept(value * 0.86));
            releaseClient.download(
                    release.signature(),
                    signature,
                    MAX_SIGNATURE_SIZE,
                    value -> progress.accept(0.86 + value * 0.02));

            UpdatePackage.verifySignature(archive, signature);
            progress.accept(0.91);

            Path extracted = stagingRoot.resolve("extracted");
            Path payload = UpdatePackage.extractAndValidate(
                    archive,
                    extracted,
                    release.version());
            progress.accept(0.97);

            launchElevatedInstaller(
                    extracted.resolve("cman_updater.vbs"),
                    payload,
                    resolveInstallDirectory(),
                    stagingRoot,
                    release.version());
            installerStarted = true;
            progress.accept(1.0);
        } finally {
            if (!installerStarted) {
                UpdatePackage.deleteRecursively(stagingRoot);
            }
        }
    }

    static void signalSuccessfulStart(List<String> arguments) {
        if (arguments == null) return;

        for (String argument : arguments) {
            if (!argument.startsWith(SUCCESS_ARGUMENT_PREFIX)) continue;

            String rawPath = argument.substring(SUCCESS_ARGUMENT_PREFIX.length());
            try {
                Path marker = Path.of(rawPath).toAbsolutePath().normalize();
                if (!"new-version-started.marker".equals(marker.getFileName().toString())) {
                    return;
                }
                Files.writeString(
                        marker,
                        AppConstants.APP_VERSION,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Could not confirm successful update start", exception);
            }
            return;
        }
    }

    private static void launchElevatedInstaller(
            Path script,
            Path payload,
            Path installDirectory,
            Path stagingRoot,
            SemanticVersion version) throws IOException {

        Path readyMarker = stagingRoot.resolve("elevation-approved.marker");
        Path cancelledMarker = stagingRoot.resolve("elevation-cancelled.marker");
        Path successMarker = stagingRoot.resolve("new-version-started.marker");

        Process process = new ProcessBuilder(
                "wscript.exe",
                script.toString(),
                payload.toString(),
                installDirectory.toString(),
                readyMarker.toString(),
                cancelledMarker.toString(),
                successMarker.toString(),
                Long.toString(ProcessHandle.current().pid()),
                version.toString())
                .start();

        Instant deadline = Instant.now().plus(ELEVATION_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            if (Files.exists(readyMarker)) return;
            if (Files.exists(cancelledMarker)) {
                throw new IOException("Update installation was cancelled");
            }

            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                process.destroy();
                throw new IOException("Waiting for administrator permission was interrupted", exception);
            }
        }

        throw new IOException(
                "Administrator permission was not granted within "
                        + ELEVATION_TIMEOUT.toSeconds() + " seconds");
    }

    private static Path resolveInstallDirectory() throws IOException {
        String launcherPath = System.getProperty("jpackage.app-path");
        if (launcherPath != null && !launcherPath.isBlank()) {
            Path launcher = Path.of(launcherPath).toAbsolutePath().normalize();
            Path parent = launcher.getParent();
            if (parent != null && Files.isRegularFile(parent.resolve("CourseManagerFX.exe"))) {
                return parent;
            }
        }

        try {
            Path codeLocation = Path.of(UpdateInstaller.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .toAbsolutePath()
                    .normalize();

            if (Files.isRegularFile(codeLocation)) {
                Path parent = codeLocation.getParent();
                if (parent != null
                        && "app".equalsIgnoreCase(parent.getFileName().toString())
                        && parent.getParent() != null) {
                    parent = parent.getParent();
                }
                if (parent != null && Files.isRegularFile(parent.resolve("CourseManagerFX.exe"))) {
                    return parent;
                }
            }
        } catch (URISyntaxException exception) {
            throw new IOException("Could not locate the application installation", exception);
        }

        throw new IOException(
                "Automatic updates work only from an installed CourseManagerFX app image");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT)
                .startsWith("windows");
    }
}
