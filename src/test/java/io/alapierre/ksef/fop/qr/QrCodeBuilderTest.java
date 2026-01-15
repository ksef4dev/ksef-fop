package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.InvoiceQRCodeGeneratorRequest;
import io.alapierre.ksef.fop.i18n.TranslationService;
import io.alapierre.ksef.fop.qr.enums.ContextIdentifierType;
import io.alapierre.ksef.fop.qr.helpers.CertificateBuilders;
import io.alapierre.ksef.fop.qr.helpers.SelfSignedCertificate;
import io.alapierre.ksef.fop.qr.helpers.TestCertificateGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeBuilderTest {

    private QrCodeBuilder qrCodeBuilder;
    private byte[] testInvoiceXml;

    @BeforeEach
    void setUp() throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        TranslationService translationService = new TranslationService();
        qrCodeBuilder = new QrCodeBuilder(translationService);
        testInvoiceXml = Files.readAllBytes(Paths.get("src/test/resources/faktury/fa3/podstawowa/FA_3_Przyklad_1.xml"));
    }

    @Test
    void buildOnlineQr_withKsefNumber_shouldUseKsefNumberAsLabel() {
        String ksefNumber = "6891152920-20251008-010000B4CF64-9C";
        InvoiceQRCodeGeneratorRequest req = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(
                "https://qr-test.ksef.mf.gov.pl", "6891152920", LocalDate.of(2025, 10, 8));

        QrCodeData result = qrCodeBuilder.buildOnlineQr(req, ksefNumber, testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(ksefNumber, result.getLabel());
        assertNotNull(result.getVerificationLink());
        assertNotNull(result.getVerificationLinkTitle());
        assertNotNull(result.getQrCodeImage());
        assertTrue(result.getQrCodeImage().length > 0);
        assertTrue(result.getVerificationLink().contains("https://qr-test.ksef.mf.gov.pl"));
    }

    @Test
    void buildOnlineQr_withoutKsefNumber_shouldUseOfflineLabel() {
        InvoiceQRCodeGeneratorRequest req = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(
                "https://qr-test.ksef.mf.gov.pl", "6891152920", LocalDate.of(2025, 10, 8));

        QrCodeData result = qrCodeBuilder.buildOnlineQr(req, null, testInvoiceXml, "pl");

        assertNotNull(result);
        assertNotEquals("", result.getLabel());
        assertNotNull(result.getVerificationLink());
        assertNotNull(result.getVerificationLinkTitle());
        assertNotNull(result.getQrCodeImage());
    }

    @Test
    void buildOnlineQr_shouldUseCorrectLanguage() {
        InvoiceQRCodeGeneratorRequest req = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(
                "https://qr-test.ksef.mf.gov.pl", "6891152920", LocalDate.of(2025, 10, 8));

        QrCodeData resultPl = qrCodeBuilder.buildOnlineQr(req, null, testInvoiceXml, "pl");
        QrCodeData resultEn = qrCodeBuilder.buildOnlineQr(req, null, testInvoiceXml, "en");

        assertNotNull(resultPl.getVerificationLinkTitle());
        assertNotNull(resultEn.getVerificationLinkTitle());
        // Titles should be different for different languages
        assertNotEquals(resultPl.getVerificationLinkTitle(), resultEn.getVerificationLinkTitle());
    }

    @Test
    void buildCertificateQr_shouldGenerateValidQrCode() throws Exception {
        CertificateBuilders.X500NameHolder x500 = new CertificateBuilders()
                .buildForOrganization("Test Org", "VATPL-1234567890", "TestCN", "PL");
        TestCertificateGenerator generator = new TestCertificateGenerator();
        SelfSignedCertificate cert = generator.generateSelfSignedCertificateEcdsa(x500);

        InvoiceQRCodeGeneratorRequest req = InvoiceQRCodeGeneratorRequest.offlineCertificateQrBuilder(
                "https://qr-test.ksef.mf.gov.pl",
                ContextIdentifierType.NIP,
                "6891152920",
                "6891152920",
                "01F20A5D352AE590",
                cert.getPrivateKey(),
                LocalDate.of(2025, 10, 8));

        QrCodeData result = qrCodeBuilder.buildCertificateQr(req, testInvoiceXml, "pl");

        assertNotNull(result);
        assertNotNull(result.getLabel());
        assertNotNull(result.getVerificationLink());
        assertNotNull(result.getVerificationLinkTitle());
        assertNotNull(result.getQrCodeImage());
        assertTrue(result.getQrCodeImage().length > 0);
        assertTrue(result.getVerificationLink().contains("https://qr-test.ksef.mf.gov.pl"));
        assertTrue(result.getVerificationLink().contains("/certificate/"));
    }

    @Test
    void buildCertificateQr_shouldUseCorrectLanguage() throws Exception {
        CertificateBuilders.X500NameHolder x500 = new CertificateBuilders()
                .buildForOrganization("Test Org", "1234567890", "TestCN", "PL");
        TestCertificateGenerator certGenerator = new TestCertificateGenerator();
        SelfSignedCertificate cert = certGenerator.generateSelfSignedCertificateEcdsa(x500);

        InvoiceQRCodeGeneratorRequest req = InvoiceQRCodeGeneratorRequest.offlineCertificateQrBuilder(
                "https://qr-test.ksef.mf.gov.pl",
                ContextIdentifierType.NIP,
                "6891152920",
                "6891152920",
                "01F20A5D352AE590",
                cert.getPrivateKey(),
                LocalDate.of(2025, 10, 8));

        QrCodeData resultPl = qrCodeBuilder.buildCertificateQr(req, testInvoiceXml, "pl");
        QrCodeData resultEn = qrCodeBuilder.buildCertificateQr(req, testInvoiceXml, "en");

        assertNotNull(resultPl.getVerificationLinkTitle());
        assertNotNull(resultEn.getVerificationLinkTitle());
        // Titles should be different for different languages
        assertNotEquals(resultPl.getVerificationLinkTitle(), resultEn.getVerificationLinkTitle());
    }

    @Test
    void qrFromLink_shouldCreateValidQrCodeData() {
        String testLink = "https://qr-test.ksef.mf.gov.pl/web/verify/test";
        String testLabel = "Test Label";
        String testTitle = "Test Title";

        QrCodeData result = qrCodeBuilder.qrFromLink(testLink, testLabel, testTitle);

        assertNotNull(result);
        assertEquals(testLink, result.getVerificationLink());
        assertEquals(testLabel, result.getLabel());
        assertEquals(testTitle, result.getVerificationLinkTitle());
        assertNotNull(result.getQrCodeImage());
        assertTrue(result.getQrCodeImage().length > 0);
    }

    @Test
    void buildQrCodes_onlineMode_shouldReturnSingleQrCode() {
        InvoiceQRCodeGeneratorRequest req = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(
                "https://qr-test.ksef.mf.gov.pl", "6891152920", LocalDate.of(2025, 10, 8));

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(req, null, testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        assertTrue(result.get(0).getVerificationLink().contains("https://qr-test.ksef.mf.gov.pl"));
    }

    @Test
    void buildQrCodes_withNullRequest_shouldReturnNull() {
        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(null, null, testInvoiceXml, "pl");

        assertNull(result);}

}
