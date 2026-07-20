package com.coursemanagerfx.logic.utilities.update;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.logic.utilities.update.ReleaseInfo.ReleaseAsset;
import com.coursemanagerfx.logic.utilities.update.exceptions.NoInternetConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

final class GitHubReleaseClient {

    private static final String ACCEPT = "application/vnd.github+json";
    private static final String API_VERSION = "2026-03-10";
    private static final long MAX_ARCHIVE_SIZE = 512L * 1024L * 1024L;
    private static final long MAX_SIGNATURE_SIZE = 4L * 1024L;

    private final Gson gson;
    private final HttpClient httpClient;
    private final URI latestReleaseUri;

    GitHubReleaseClient() {
        this(
                new Gson(),
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                URI.create("https://api.github.com/repos/"
                        + AppConstants.GITHUB_REPOSITORY + "/releases/latest"));
    }

    GitHubReleaseClient(Gson gson, HttpClient httpClient, URI latestReleaseUri) {
        this.gson = Objects.requireNonNull(gson, "gson");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.latestReleaseUri = Objects.requireNonNull(latestReleaseUri, "latestReleaseUri");
    }

    ReleaseInfo fetchLatestRelease() {
        HttpRequest request = baseRequest(latestReleaseUri)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Update check was interrupted", exception);
        } catch (IOException exception) {
            throw mapNetworkFailure("Could not connect to GitHub", exception);
        }

        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IllegalStateException(
                    "No stable GitHub Release is marked as Latest");
        }
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new IllegalStateException(
                    "GitHub update check failed with HTTP " + response.statusCode());
        }

        ApiRelease release;
        try {
            release = gson.fromJson(response.body(), ApiRelease.class);
        } catch (JsonParseException exception) {
            throw new IllegalStateException("GitHub returned invalid release data", exception);
        }
        if (release == null || release.tagName == null || release.htmlUrl == null) {
            throw new IllegalStateException("GitHub returned incomplete release data");
        }
        if (release.draft || release.prerelease) {
            throw new IllegalStateException("The Latest release must be stable and published");
        }

        SemanticVersion version = SemanticVersion.parse(release.tagName);
        String archiveName = "CourseManagerFX_v" + version + ".zip";
        String signatureName = archiveName + ".sig";

        ReleaseAsset archive = findAsset(release.assets, archiveName, MAX_ARCHIVE_SIZE);
        ReleaseAsset signature = findAsset(release.assets, signatureName, MAX_SIGNATURE_SIZE);

        return new ReleaseInfo(version, URI.create(release.htmlUrl), archive, signature);
    }

    void download(
            ReleaseAsset asset,
            Path target,
            long maximumSize,
            Consumer<Double> progressCallback) throws IOException {

        Objects.requireNonNull(asset, "asset");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(progressCallback, "progressCallback");

        if (asset.size() > maximumSize) {
            throw new IOException("Release asset is larger than allowed: " + asset.name());
        }

        HttpRequest request = HttpRequest.newBuilder(asset.downloadUrl())
                .timeout(Duration.ofMinutes(5))
                .header("Accept", "application/octet-stream")
                .header("User-Agent", AppConstants.APP_NAME + "/" + AppConstants.APP_VERSION)
                .GET()
                .build();

        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Download was interrupted", exception);
        } catch (IOException exception) {
            throw mapNetworkFailure("Could not download " + asset.name(), exception);
        }

        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            response.body().close();
            throw new IOException(
                    "Download of " + asset.name() + " failed with HTTP " + response.statusCode());
        }

        long responseLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
        long expectedLength = responseLength >= 0 ? responseLength : asset.size();
        if (responseLength > maximumSize) {
            response.body().close();
            throw new IOException("Downloaded asset is larger than allowed: " + asset.name());
        }

        Files.createDirectories(target.toAbsolutePath().normalize().getParent());
        try (InputStream input = response.body();
             OutputStream output = Files.newOutputStream(
                     target,
                     StandardOpenOption.CREATE_NEW,
                     StandardOpenOption.WRITE)) {

            byte[] buffer = new byte[64 * 1024];
            long total = 0L;
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read == 0) continue;
                total += read;
                if (total > maximumSize) {
                    throw new IOException("Downloaded asset is larger than allowed: " + asset.name());
                }
                output.write(buffer, 0, read);
                if (expectedLength > 0) {
                    progressCallback.accept(Math.min(1.0, total / (double) expectedLength));
                }
            }

            if (asset.size() > 0 && total != asset.size()) {
                throw new IOException(
                        "Downloaded size does not match GitHub metadata for " + asset.name());
            }
            progressCallback.accept(1.0);
        } catch (IOException exception) {
            Files.deleteIfExists(target);
            throw exception;
        } catch (RuntimeException exception) {
            Files.deleteIfExists(target);
            throw exception;
        }
    }

    private HttpRequest.Builder baseRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
                .header("Accept", ACCEPT)
                .header("X-GitHub-Api-Version", API_VERSION)
                .header("User-Agent", AppConstants.APP_NAME + "/" + AppConstants.APP_VERSION);
    }

    private static ReleaseAsset findAsset(
            List<ApiAsset> assets,
            String expectedName,
            long maximumSize) {

        if (assets == null) {
            throw new IllegalStateException("Release has no assets");
        }

        ApiAsset match = assets.stream()
                .filter(asset -> expectedName.equals(asset.name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Required release asset is missing: " + expectedName));

        if (match.browserDownloadUrl == null || match.size < 0 || match.size > maximumSize) {
            throw new IllegalStateException("Invalid release asset: " + expectedName);
        }

        return new ReleaseAsset(
                match.name,
                URI.create(match.browserDownloadUrl),
                match.size);
    }

    private static RuntimeException mapNetworkFailure(String message, IOException exception) {
        if (hasCause(exception, UnknownHostException.class)
                || hasCause(exception, ConnectException.class)) {
            return new NoInternetConnection(message, exception);
        }
        return new IllegalStateException(message, exception);
    }

    private static boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) return true;
            current = current.getCause();
        }
        return false;
    }

    private static final class ApiRelease {
        @com.google.gson.annotations.SerializedName("tag_name")
        private String tagName;

        @com.google.gson.annotations.SerializedName("html_url")
        private String htmlUrl;

        private boolean draft;
        private boolean prerelease;
        private List<ApiAsset> assets;
    }

    private static final class ApiAsset {
        private String name;

        @com.google.gson.annotations.SerializedName("browser_download_url")
        private String browserDownloadUrl;

        private long size;
    }
}
