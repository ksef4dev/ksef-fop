package io.alapierre.ksef.fop.qr;


import io.alapierre.ksef.fop.qr.enums.ContextIdentifierType;
import io.alapierre.ksef.fop.qr.helpers.CertificateBuilders;
import io.alapierre.ksef.fop.qr.helpers.SelfSignedCertificate;
import io.alapierre.ksef.fop.qr.helpers.TestCertificateGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationLinkGeneratorTest {

    private static final DateTimeFormatter KSEF_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    @Test
    void generateOnlineVerificationLink() throws IOException {
        File invoiceFile = new File("src/test/resources/faktury/fa3/podstawowa/FA_3_Przyklad_1.xml");
        byte[] invoiceXml = Files.readAllBytes(invoiceFile.toPath());
        String identifier = "6891152920";
        LocalDate issueDate = LocalDate.of(2025, 10, 8);
        String verificationLink = VerificationLinkGenerator.generateVerificationLink(
                "https://ksef-test.mf.gov.pl",
                identifier,
                issueDate,
                invoiceXml);

        assertTrue(verificationLink.contains("https://ksef-test.mf.gov.pl"));
        assertTrue(verificationLink.contains(identifier));
        assertTrue(verificationLink.contains(issueDate.format(KSEF_DATE)));
        assertTrue(verificationLink.contains(CryptoUtils.computeInvoiceHashBase64Url(invoiceXml)));

        System.out.println(verificationLink);
    }

    @Test
    void generateCertificateVerificationLinkWithRSA() throws Exception {
        File invoiceFile = new File("src/test/resources/faktury/fa3/podstawowa/FA_3_Przyklad_1.xml");
        byte[] invoiceXml = Files.readAllBytes(invoiceFile.toPath());
        String identifier = "6891152920";
        String serial = "01F20A5D352AE590"; // Example certificate serial in hex format

        // Generate RSA key pair for testing using utility class
        CertificateBuilders.X500NameHolder x500 = new CertificateBuilders()
                .buildForOrganization("Kowalski sp. z o.o", "VATPL-1111111111", "TestEcc", "PL");
        SelfSignedCertificate cert = new TestCertificateGenerator().generateSelfSignedCertificateEcdsa(x500);

        String verificationLink = VerificationLinkGenerator.generateCertificateVerificationLink(
                "https://qr-test.ksef.mf.gov.pl",
                ContextIdentifierType.NIP,
                identifier,
                identifier,
                serial,
                cert.getPrivateKey(),
                invoiceXml);

        // Verify the link structure
        assertTrue(verificationLink.contains("https://qr-test.ksef.mf.gov.pl"));
        assertTrue(verificationLink.contains(identifier));
        assertTrue(verificationLink.contains(serial));
        assertTrue(verificationLink.contains(CryptoUtils.computeInvoiceHashBase64Url(invoiceXml)));

        // Verify it contains a signature (Base64URL encoded)
        String[] parts = verificationLink.split("/");
        String signature = parts[parts.length - 1];
        assertFalse(signature.isEmpty());
        assertTrue(signature.matches("[A-Za-z0-9_-]+"), "Signature should be Base64URL encoded");

        System.out.println("Certificate verification link: " + verificationLink);
    }

    @Test
    void generateCertificateVerificationLinkWithECDSA() throws Exception {
        File invoiceFile = new File("src/test/resources/faktury/fa3/podstawowa/FA_3_Przyklad_1.xml");
        byte[] invoiceXml = Files.readAllBytes(invoiceFile.toPath());
        String identifier = "6891152920";
        String serial = "01F20A5D352AE590";

        // Generate RSA key pair for testing using utility class
        CertificateBuilders.X500NameHolder x500 = new CertificateBuilders()
                .buildForOrganization("Kowalski sp. z o.o", "VATPL-1111111111", "TestEcc", "PL");
        SelfSignedCertificate cert = new TestCertificateGenerator().generateSelfSignedCertificateEcdsa(x500);

        String verificationLink = VerificationLinkGenerator.generateCertificateVerificationLink(
                "https://qr-test.ksef.mf.gov.pl",
                ContextIdentifierType.NIP,
                identifier,
                identifier,
                serial,
                cert.getPrivateKey(),
                invoiceXml);

        // Verify the link structure
        assertTrue(verificationLink.contains("https://qr-test.ksef.mf.gov.pl"));
        assertTrue(verificationLink.contains(identifier));
        assertTrue(verificationLink.contains(serial));
        assertTrue(verificationLink.contains(CryptoUtils.computeInvoiceHashBase64Url(invoiceXml)));
        assertTrue(verificationLink.contains("/certificate/"));

        // Verify it contains a signature (Base64URL encoded)
        String[] parts = verificationLink.split("/");
        String signature = parts[parts.length - 1];
        assertFalse(signature.isEmpty());
        assertTrue(signature.matches("[A-Za-z0-9_-]+"), "Signature should be Base64URL encoded");

        System.out.println("ECDSA Certificate verification link: " + verificationLink);
    }

}