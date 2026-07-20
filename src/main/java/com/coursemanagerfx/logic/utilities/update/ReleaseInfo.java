package com.coursemanagerfx.logic.utilities.update;

import java.net.URI;
import java.util.Objects;

/** The stable GitHub Release and the two assets required by the updater. */
public record ReleaseInfo(
        SemanticVersion version,
        URI pageUrl,
        ReleaseAsset archive,
        ReleaseAsset signature) {

    public ReleaseInfo {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(pageUrl, "pageUrl");
        Objects.requireNonNull(archive, "archive");
        Objects.requireNonNull(signature, "signature");
    }

    public record ReleaseAsset(String name, URI downloadUrl, long size) {
        public ReleaseAsset {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Release asset name cannot be blank");
            }
            Objects.requireNonNull(downloadUrl, "downloadUrl");
            if (size < 0) {
                throw new IllegalArgumentException("Release asset size cannot be negative");
            }
        }
    }
}
