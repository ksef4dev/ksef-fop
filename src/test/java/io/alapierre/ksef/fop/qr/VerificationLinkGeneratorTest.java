package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.qr.enums.ContextIdentifierType;
import io.alapierre.ksef.fop.qr.enums.Environment;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationLinkGeneratorTest {

    private static final DateTimeFormatter KSEF_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String IDENTIFIER = "3731383632";
    private static final Environment TEST_ENV = Environment.TEST;

    private static final String INVOICE_NIP_XML = "faktury/fa3/qr_test/nip.xml";
    private static final String INVOICE_OSOBA_FIZYCZNA_XML = "faktury/fa3/qr_test/osoba_fizyczna.xml";

    //  Uwaga!
    //  Poniższe certyfikaty, klucze prywatne oraz hasła są używane
    //  WYŁĄCZNIE do testów w środowisku testowym KSeF.
    //
    //  - dane zostały wygenerowane tylko na potrzeby testów,
    //  - nie mają związku z żadnym realnym podmiotem,
    //  - NIE wolno używać ich ani przechowywać w aplikacjach produkcyjnych,
    //  - NIE wolno kopiować ich do repozytoriów produkcyjnych,
    //  - NIE wolno używać ich poza lokalnym/testowym środowiskiem CI.
    //
    //  Certyfikaty te nie dają żadnego dostępu do systemu KSeF
    //  i mogą być bezpiecznie ujawnione WYŁĄCZNIE w kontekście testów.
    private static final String PESEL_CERT_PATH = "certs/pesel_sign.crt";
    private static final String PESEL_KEY_PATH = "certs/pesel_sign.key";
    private static final char[] PESEL_PASSWORD = "Izqg^8!XOcq3E5N5".toCharArray();

    private static final String NIP_CERT_PATH = "certs/nip_sign.crt";
    private static final String NIP_KEY_PATH = "certs/nip_sign.key";
    private static final char[] NIP_PASSWORD = "Lu@P2@3DFDHo@up0".toCharArray();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void generateOnlineVerificationLink() throws Exception {
        byte[] invoiceXml = loadResourceBytes(INVOICE_NIP_XML);
        LocalDate issueDate = LocalDate.of(2025, 11, 13);

        String verificationLink = VerificationLinkGenerator.generateVerificationLink(
                TEST_ENV,
                IDENTIFIER,
                issueDate,
                invoiceXml
        );

        assertOnlineVerificationLink(verificationLink, issueDate, invoiceXml);
        System.out.println(verificationLink);
    }

    @Test
    void generateCertificateVerificationLinkWithRealEncryptedKeyForAuthorizedPerson() throws Exception {
        byte[] invoiceXml = loadResourceBytes(INVOICE_OSOBA_FIZYCZNA_XML);

        VerificationResult result = generateCertificateVerificationLink(
                invoiceXml,
                PESEL_CERT_PATH,
                PESEL_KEY_PATH,
                PESEL_PASSWORD
        );

        assertCertificateVerificationLink(result.link(), result.serial(), invoiceXml);
        System.out.println("Real certificate verification link (authorized person, encrypted key): " + result.link());
    }

    @Test
    void generateCertificateVerificationLinkWithRealEncryptedKeyForContextOwner() throws Exception {
        byte[] invoiceXml = loadResourceBytes(INVOICE_NIP_XML);

        VerificationResult result = generateCertificateVerificationLink(
                invoiceXml,
                NIP_CERT_PATH,
                NIP_KEY_PATH,
                NIP_PASSWORD
        );

        assertCertificateVerificationLink(result.link(), result.serial(), invoiceXml);
        System.out.println("Real certificate verification link (context owner, encrypted key): " + result.link());
    }


    private byte[] loadResourceBytes(String path) throws IOException {
        ClassLoader cl = getClass().getClassLoader();
        try (InputStream is = cl.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found on classpath: " + path);
            }
            return is.readAllBytes();
        }
    }

    private X509Certificate loadCertificate(String path) throws Exception {
        byte[] certBytes = loadResourceBytes(path);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private PrivateKey loadEncryptedPrivateKeyFromClasspath(String path, char[] password) throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        try (InputStream is = cl.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Key resource not found on classpath: " + path);
            }
            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                 PEMParser pemParser = new PEMParser(reader)) {

                Object obj = pemParser.readObject();
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

                return switch (obj) {
                    case PKCS8EncryptedPrivateKeyInfo encPkcs8 -> {
                        var decryptorProvider =
                                new JceOpenSSLPKCS8DecryptorProviderBuilder()
                                        .setProvider("BC")
                                        .build(password);
                        PrivateKeyInfo pkInfo = encPkcs8.decryptPrivateKeyInfo(decryptorProvider);
                        yield converter.getPrivateKey(pkInfo);
                    }
                    case PEMEncryptedKeyPair encKeyPair -> {
                        PEMDecryptorProvider decProv =
                                new JcePEMDecryptorProviderBuilder()
                                        .setProvider("BC")
                                        .build(password);
                        PEMKeyPair keyPair = encKeyPair.decryptKeyPair(decProv);
                        yield converter.getKeyPair(keyPair).getPrivate();
                    }
                    case PEMKeyPair keyPair -> converter.getKeyPair(keyPair).getPrivate();
                    default -> throw new IllegalArgumentException("Unsupported key format: " + obj.getClass());
                };
            }
        }
    }

    private VerificationResult generateCertificateVerificationLink(byte[] invoiceXml,
                                                                   String certPath,
                                                                   String keyPath,
                                                                   char[] password) throws Exception {

        X509Certificate cert = loadCertificate(certPath);
        String serial = extractCertSerial(cert);
        PrivateKey privateKey = loadEncryptedPrivateKeyFromClasspath(keyPath, password);

        String link = VerificationLinkGenerator.generateCertificateVerificationLink(
                TEST_ENV,
                ContextIdentifierType.NIP,
                IDENTIFIER,
                IDENTIFIER,
                serial,
                privateKey,
                invoiceXml
        );

        return new VerificationResult(link, serial);
    }

    private void assertOnlineVerificationLink(String verificationLink,
                                              LocalDate issueDate,
                                              byte[] invoiceXml) {

        assertTrue(verificationLink.contains(TEST_ENV.getUrl()));
        assertTrue(verificationLink.contains(IDENTIFIER));
        assertTrue(verificationLink.contains(issueDate.format(KSEF_DATE)));
        assertTrue(verificationLink.contains(CryptoUtils.computeInvoiceHashBase64Url(invoiceXml)));
    }

    private void assertCertificateVerificationLink(String verificationLink,
                                                   String serial,
                                                   byte[] invoiceXml) {

        assertTrue(verificationLink.contains(TEST_ENV.getUrl()));
        assertTrue(verificationLink.contains(IDENTIFIER));
        assertTrue(verificationLink.contains(serial));
        assertTrue(verificationLink.contains(CryptoUtils.computeInvoiceHashBase64Url(invoiceXml)));
        assertTrue(verificationLink.contains("/client-app/certificate/"));

        String[] parts = verificationLink.split("/");
        String signature = parts[parts.length - 1];
        assertFalse(signature.isEmpty());
        assertTrue(signature.matches("[A-Za-z0-9_-]+"), "Signature should be Base64URL encoded");
    }

    public static String extractCertSerial(X509Certificate cert) {
        byte[] serialBytes = cert.getSerialNumber().toByteArray();

        if (serialBytes.length > 1 && serialBytes[0] == 0x00) {
            serialBytes = Arrays.copyOfRange(serialBytes, 1, serialBytes.length);
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : serialBytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    private record VerificationResult(String link, String serial) {}
}