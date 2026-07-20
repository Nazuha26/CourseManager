package com.coursemanagerfx.logic.utilities.update;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Compatibility facade used by the existing UI. Network access, verification,
 * extraction and installation are implemented by focused package classes.
 */
public final class UpdateUtility {

    private static final GitHubReleaseClient RELEASE_CLIENT = new GitHubReleaseClient();
    private static final UpdateInstaller INSTALLER = new UpdateInstaller(RELEASE_CLIENT);
    private static final AtomicReference<ReleaseInfo> CACHED_RELEASE = new AtomicReference<>();
    private static final AtomicBoolean AUTOMATIC_CHECK_STARTED = new AtomicBoolean();
    private static final Object CHECK_LOCK = new Object();

    private UpdateUtility() {
    }

    /** Returns the version of GitHub's latest stable release. */
    public static String getLatestGitVersion() {
        ReleaseInfo release = CACHED_RELEASE.get();
        if (release == null) {
            synchronized (CHECK_LOCK) {
                release = CACHED_RELEASE.get();
                if (release == null) {
                    release = RELEASE_CLIENT.fetchLatestRelease();
                    CACHED_RELEASE.set(release);
                }
            }
        }
        return release.version().toString();
    }

    /** Downloads, verifies and starts installation of the latest stable release. */
    public static void installUpdate(
            String version,
            Consumer<Double> progressCallback) throws IOException {

        SemanticVersion requestedVersion = SemanticVersion.parse(version);
        ReleaseInfo release = CACHED_RELEASE.get();

        if (release == null || !release.version().equals(requestedVersion)) {
            release = RELEASE_CLIENT.fetchLatestRelease();
            CACHED_RELEASE.set(release);
        }
        if (!release.version().equals(requestedVersion)) {
            throw new IOException(
                    "Requested update v" + requestedVersion
                            + " is no longer the Latest GitHub Release");
        }

        INSTALLER.prepareAndLaunch(release, progressCallback);
    }

    public static int compareVersions(String first, String second) {
        return SemanticVersion.parse(first).compareTo(SemanticVersion.parse(second));
    }

    /** Returns {@code true} only for the first automatic check in this process. */
    public static boolean beginAutomaticCheck() {
        return AUTOMATIC_CHECK_STARTED.compareAndSet(false, true);
    }

    /** Called after the new application window has been opened successfully. */
    public static void signalSuccessfulStart(List<String> arguments) {
        UpdateInstaller.signalSuccessfulStart(arguments);
    }
}
