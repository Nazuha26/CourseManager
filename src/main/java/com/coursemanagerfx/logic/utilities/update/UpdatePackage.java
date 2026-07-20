package com.coursemanagerfx.logic.utilities.update;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class UpdatePackage {

    private static final String PUBLIC_KEY_RESOURCE =
            "/com/coursemanagerfx/update-public-key.der";
    private static final long MAX_ENTRY_SIZE = 512L * 1024L * 1024L;
    private static final long MAX_TOTAL_SIZE = 1024L * 1024L * 1024L;
    private static final int MAX_ENTRY_COUNT = 20_000;
    private static final int ED25519_SIGNATURE_SIZE = 64;

    private UpdatePackage() {
    }

    static void verifySignature(Path archive, Path signatureFile) throws IOException {
        verifySignature(archive, signatureFile, loadPublicKey());
    }

    static void verifySignature(
            Path archive,
            Path signatureFile,
            PublicKey publicKey) throws IOException {

        byte[] signatureBytes = Files.readAllBytes(signatureFile);
        if (signatureBytes.length != ED25519_SIGNATURE_SIZE) {
            throw new IOException("Update signature has an invalid size");
        }

        try {
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(publicKey);

            try (InputStream input = Files.newInputStream(archive)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read > 0) verifier.update(buffer, 0, read);
                }
            }

            if (!verifier.verify(signatureBytes)) {
                throw new IOException(
                        "Update signature is invalid. The package will not be installed.");
            }
        } catch (GeneralSecurityException exception) {
            throw new IOException("Could not verify the update signature", exception);
        }
    }

    static Path extractAndValidate(
            Path archive,
            Path extractionDirectory,
            SemanticVersion expectedVersion) throws IOException {

        Files.createDirectory(extractionDirectory);
        Path normalizedRoot = extractionDirectory.toAbsolutePath().normalize();
        Set<String> entryNames = new HashSet<>();
        long totalSize = 0L;
        int entryCount = 0;

        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            byte[] buffer = new byte[64 * 1024];

            while ((entry = zip.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_ENTRY_COUNT) {
                    throw new IOException("Update archive contains too many files");
                }

                String entryName = validateEntryName(entry.getName());
                String foldedName = entryName.toLowerCase(Locale.ROOT);
                if (!entryNames.add(foldedName)) {
                    throw new IOException("Duplicate update archive entry: " + entryName);
                }
                if (entry.getSize() > MAX_ENTRY_SIZE) {
                    throw new IOException("Update archive entry is too large: " + entryName);
                }

                Path output = normalizedRoot.resolve(entryName).normalize();
                if (!output.startsWith(normalizedRoot)) {
                    throw new IOException("Unsafe update archive entry: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Path parent = output.getParent();
                    if (parent == null || !parent.startsWith(normalizedRoot)) {
                        throw new IOException("Unsafe update archive entry: " + entryName);
                    }
                    Files.createDirectories(parent);

                    long entrySize = 0L;
                    try (OutputStream file = Files.newOutputStream(
                            output,
                            StandardOpenOption.CREATE_NEW,
                            StandardOpenOption.WRITE)) {

                        int read;
                        while ((read = zip.read(buffer)) >= 0) {
                            if (read == 0) continue;
                            entrySize += read;
                            totalSize += read;
                            if (entrySize > MAX_ENTRY_SIZE || totalSize > MAX_TOTAL_SIZE) {
                                throw new IOException("Update archive is too large when extracted");
                            }
                            file.write(buffer, 0, read);
                        }
                    }
                }
                zip.closeEntry();
            }
        } catch (IOException | RuntimeException exception) {
            deleteRecursively(extractionDirectory);
            throw exception;
        }

        try {
            validateLayout(normalizedRoot, expectedVersion);
            return normalizedRoot.resolve("payload");
        } catch (IOException | RuntimeException exception) {
            deleteRecursively(extractionDirectory);
            throw exception;
        }
    }

    private static PublicKey loadPublicKey() throws IOException {
        try (InputStream input = UpdatePackage.class.getResourceAsStream(PUBLIC_KEY_RESOURCE)) {
            if (input == null) {
                throw new IOException("Embedded update verification key is missing");
            }
            byte[] encoded = input.readAllBytes();
            return KeyFactory.getInstance("Ed25519")
                    .generatePublic(new X509EncodedKeySpec(encoded));
        } catch (GeneralSecurityException exception) {
            throw new IOException("Embedded update verification key is invalid", exception);
        }
    }

    private static String validateEntryName(String name) throws IOException {
        if (name == null || name.isBlank()
                || name.startsWith("/")
                || name.indexOf('\\') >= 0
                || name.indexOf('\0') >= 0
                || name.indexOf(':') >= 0) {
            throw new IOException("Unsafe update archive entry: " + name);
        }
        return name;
    }

    private static void validateLayout(Path root, SemanticVersion expectedVersion)
            throws IOException {

        Set<String> allowedTopLevel = Set.of(
                "update.properties",
                "cman_updater.vbs",
                "payload");

        try (Stream<Path> entries = Files.list(root)) {
            Path unexpected = entries
                    .filter(path -> !allowedTopLevel.contains(path.getFileName().toString()))
                    .findFirst()
                    .orElse(null);
            if (unexpected != null) {
                throw new IOException(
                        "Unexpected top-level update file: " + unexpected.getFileName());
            }
        }

        Path propertiesFile = requireRegularFile(root.resolve("update.properties"));
        requireRegularFile(root.resolve("cman_updater.vbs"));
        Path payload = root.resolve("payload");
        if (!Files.isDirectory(payload)) {
            throw new IOException("Update payload directory is missing");
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        if (!"1".equals(properties.getProperty("format"))) {
            throw new IOException("Unsupported update package format");
        }

        SemanticVersion packageVersion;
        try {
            packageVersion = SemanticVersion.parse(properties.getProperty("version", ""));
        } catch (IllegalArgumentException exception) {
            throw new IOException("Update package contains an invalid version", exception);
        }
        if (!expectedVersion.equals(packageVersion)) {
            throw new IOException(
                    "Update package version does not match the GitHub Release tag");
        }

        requireRegularFile(payload.resolve("CourseManagerFX.exe"));
        requireRegularFile(payload.resolve("app").resolve("CourseManagerFX.jar"));
        requireRegularFile(payload.resolve("runtime").resolve("bin").resolve("java.exe"));
    }

    private static Path requireRegularFile(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IOException("Required update file is missing: " + file.getFileName());
        }
        return file;
    }

    static void deleteRecursively(Path directory) {
        if (directory == null || !Files.exists(directory)) return;
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted((left, right) -> right.getNameCount() - left.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Temporary update files can be removed on a later run.
                        }
                    });
        } catch (IOException ignored) {
            // Temporary update files can be removed on a later run.
        }
    }
}
