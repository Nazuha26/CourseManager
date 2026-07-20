import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class ReleaseSigner {

    private ReleaseSigner() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 3 && "generate".equalsIgnoreCase(args[0])) {
            generate(Path.of(args[1]), Path.of(args[2]));
            return;
        }
        if (args.length == 4 && "sign".equalsIgnoreCase(args[0])) {
            sign(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
            return;
        }
        if (args.length == 4 && "verify".equalsIgnoreCase(args[0])) {
            verify(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
            return;
        }

        System.err.println("Usage:");
        System.err.println("  java ReleaseSigner.java generate <private.pk8> <public.der>");
        System.err.println("  java ReleaseSigner.java sign <private.pk8> <file> <file.sig>");
        System.err.println("  java ReleaseSigner.java verify <public.der> <file> <file.sig>");
        System.exit(2);
    }

    private static void generate(Path privateFile, Path publicFile) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = generator.generateKeyPair();

        writeNew(privateFile, keyPair.getPrivate().getEncoded());
        try {
            writeNew(publicFile, keyPair.getPublic().getEncoded());
        } catch (Exception exception) {
            Files.deleteIfExists(privateFile);
            throw exception;
        }

        System.out.println("Created private key: " + privateFile.toAbsolutePath());
        System.out.println("Created public key:  " + publicFile.toAbsolutePath());
    }

    private static void sign(Path privateFile, Path input, Path signatureFile)
            throws Exception {

        PrivateKey privateKey = KeyFactory.getInstance("Ed25519")
                .generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(privateFile)));
        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(privateKey);
        update(signer, input);
        writeNew(signatureFile, signer.sign());
        System.out.println("Created signature: " + signatureFile.toAbsolutePath());
    }

    private static void verify(Path publicFile, Path input, Path signatureFile)
            throws Exception {

        PublicKey publicKey = KeyFactory.getInstance("Ed25519")
                .generatePublic(new X509EncodedKeySpec(Files.readAllBytes(publicFile)));
        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(publicKey);
        update(verifier, input);

        if (!verifier.verify(Files.readAllBytes(signatureFile))) {
            throw new IllegalStateException("Signature is invalid");
        }
        System.out.println("Signature is valid");
    }

    private static void update(Signature signature, Path input) throws Exception {
        try (InputStream stream = Files.newInputStream(input)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                if (read > 0) signature.update(buffer, 0, read);
            }
        }
    }

    private static void writeNew(Path path, byte[] bytes) throws IOException {
        Path parent = path.toAbsolutePath().normalize().getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.write(
                path,
                bytes,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
    }
}
