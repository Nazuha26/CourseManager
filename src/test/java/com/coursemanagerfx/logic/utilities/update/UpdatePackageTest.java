package com.coursemanagerfx.logic.utilities.update;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdatePackageTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void verifiesValidSignatureAndRejectsChangedArchive() throws Exception {
        Path archive = temporaryDirectory.resolve("update.zip");
        Files.writeString(archive, "trusted package", StandardCharsets.UTF_8);

        KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(keyPair.getPrivate());
        signer.update(Files.readAllBytes(archive));

        Path signature = temporaryDirectory.resolve("update.zip.sig");
        Files.write(signature, signer.sign());
        UpdatePackage.verifySignature(archive, signature, keyPair.getPublic());

        Files.writeString(archive, "changed package", StandardCharsets.UTF_8);
        assertThrows(
                IOException.class,
                () -> UpdatePackage.verifySignature(archive, signature, keyPair.getPublic()));
    }

    @Test
    void extractsAndValidatesExpectedLayout() throws Exception {
        Path archive = temporaryDirectory.resolve("valid.zip");
        createZip(archive, validEntries("1.3.0"));

        Path extraction = temporaryDirectory.resolve("valid-extracted");
        Path payload = UpdatePackage.extractAndValidate(
                archive,
                extraction,
                SemanticVersion.parse("1.3.0"));

        assertTrue(Files.isRegularFile(payload.resolve("CourseManagerFX.exe")));
        assertTrue(Files.isRegularFile(payload.resolve("app/CourseManagerFX.jar")));
        assertTrue(Files.isRegularFile(payload.resolve("runtime/bin/java.exe")));
    }

    @Test
    void rejectsZipSlipAndDoesNotWriteOutsideTarget() throws Exception {
        Path archive = temporaryDirectory.resolve("zip-slip.zip");
        Map<String, byte[]> entries = validEntries("1.3.0");
        entries.put("../outside.txt", "bad".getBytes(StandardCharsets.UTF_8));
        createZip(archive, entries);

        Path extraction = temporaryDirectory.resolve("unsafe-extracted");
        assertThrows(
                IOException.class,
                () -> UpdatePackage.extractAndValidate(
                        archive,
                        extraction,
                        SemanticVersion.parse("1.3.0")));

        assertFalse(Files.exists(temporaryDirectory.resolve("outside.txt")));
        assertFalse(Files.exists(extraction));
    }

    @Test
    void rejectsVersionMismatch() throws Exception {
        Path archive = temporaryDirectory.resolve("wrong-version.zip");
        createZip(archive, validEntries("1.3.1"));

        assertThrows(
                IOException.class,
                () -> UpdatePackage.extractAndValidate(
                        archive,
                        temporaryDirectory.resolve("wrong-version-extracted"),
                        SemanticVersion.parse("1.3.0")));
    }

    private static Map<String, byte[]> validEntries(String version) {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put(
                "update.properties",
                ("format=1\nversion=" + version + "\n").getBytes(StandardCharsets.UTF_8));
        entries.put("cman_updater.vbs", "Option Explicit\n".getBytes(StandardCharsets.UTF_8));
        entries.put("payload/CourseManagerFX.exe", new byte[] {1});
        entries.put("payload/app/CourseManagerFX.jar", new byte[] {2});
        entries.put("payload/runtime/bin/java.exe", new byte[] {3});
        return entries;
    }

    private static void createZip(Path archive, Map<String, byte[]> entries)
            throws IOException {

        try (OutputStream output = Files.newOutputStream(archive);
             ZipOutputStream zip = new ZipOutputStream(output)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
    }
}
