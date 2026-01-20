package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.InvoiceGenerationParams;
import io.alapierre.ksef.fop.InvoiceQRCodeGeneratorRequest;
import io.alapierre.ksef.fop.InvoiceSchema;
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

        assertNotNull(resultPl);
        assertNotNull(resultPl.getVerificationLinkTitle());
        assertNotNull(resultEn);
        assertNotNull(resultEn.getVerificationLinkTitle());
        // Titles should be different for different languages
        assertNotEquals(resultPl.getVerificationLinkTitle(), resultEn.getVerificationLinkTitle());
    }

    @Test
    void buildCertificateQr_shouldGenerateValidQrCode() {
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
    void buildCertificateQr_shouldUseCorrectLanguage() {
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

        assertNotNull(resultPl);
        assertNotNull(resultPl.getVerificationLinkTitle());
        assertNotNull(resultEn);
        assertNotNull(resultEn.getVerificationLinkTitle());
        // Titles should be different for different languages
        assertNotEquals(resultPl.getVerificationLinkTitle(), resultEn.getVerificationLinkTitle());
    }

    @Test
    void qrFromLink_shouldCreateValidQrCodeData() {
        String testLink = "https://qr-test.ksef.mf.gov.pl/web/verify/test";
        String testLabel = "Test Label";

        QrCodeData result = qrCodeBuilder.buildOnlineQr(testLink, testLabel, "pl");

        assertNotNull(result);
        assertEquals(testLink, result.getVerificationLink());
        assertEquals(testLabel, result.getLabel());
        assertNotNull(result.getVerificationLinkTitle());
        assertNotNull(result.getQrCodeImage());
        assertTrue(result.getQrCodeImage().length > 0);
    }

    @Test
    void buildQrCodes_withRequest_onlineMode_shouldReturnSingleQrCode() {
        InvoiceQRCodeGeneratorRequest req = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(
                "https://qr-test.ksef.mf.gov.pl", "6891152920", LocalDate.of(2025, 10, 8));

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .invoiceQRCodeGeneratorRequest(req)
                .build();

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        assertTrue(result.get(0).getVerificationLink().contains("https://qr-test.ksef.mf.gov.pl"));
    }

    @Test
    void buildOnlineQr_fromUrl_shouldGenerateQrCode() {
        String url = "https://qr-test.ksef.mf.gov.pl/invoice/6891152920/08-10-2025/abc123";
        String ksefNumber = "6891152920-20251008-010000B4CF64-9C";

        QrCodeData result = qrCodeBuilder.buildOnlineQr(url, ksefNumber, "pl");

        assertNotNull(result);
        assertEquals(url, result.getVerificationLink());
        assertEquals(ksefNumber, result.getLabel());
        assertNotNull(result.getVerificationLinkTitle());
        assertNotNull(result.getQrCodeImage());
        assertTrue(result.getQrCodeImage().length > 0);
    }

    @Test
    void buildOnlineQr_fromUrl_withoutKsefNumber_shouldUseOfflineLabel() {
        String url = "https://qr-test.ksef.mf.gov.pl/invoice/6891152920/08-10-2025/abc123";

        QrCodeData result = qrCodeBuilder.buildOnlineQr(url, null, "pl");

        assertNotNull(result);
        assertEquals(url, result.getVerificationLink());
        assertNotNull(result.getLabel());
        assertNotEquals("", result.getLabel());
        assertFalse(result.getLabel().trim().isEmpty());
    }

    @Test
    void buildOnlineQr_fromUrl_withBlankKsefNumber_shouldUseOfflineLabel() {
        String url = "https://qr-test.ksef.mf.gov.pl/invoice/6891152920/08-10-2025/abc123";

        QrCodeData result = qrCodeBuilder.buildOnlineQr(url, "   ", "pl");

        assertNotNull(result);
        assertNotNull(result.getLabel());
        assertFalse(result.getLabel().trim().isEmpty());
    }

    @Test
    void buildOnlineQr_fromUrl_shouldTrimUrl() {
        String url = "  https://qr-test.ksef.mf.gov.pl/invoice/test  ";
        String expectedUrl = "https://qr-test.ksef.mf.gov.pl/invoice/test";

        QrCodeData result = qrCodeBuilder.buildOnlineQr(url, null, "pl");

        assertNotNull(result);
        assertEquals(expectedUrl, result.getVerificationLink());
    }

    @Test
    void buildOnlineQr_fromUrl_shouldUseCorrectLanguage() {
        String url = "https://qr-test.ksef.mf.gov.pl/invoice/test";

        QrCodeData resultPl = qrCodeBuilder.buildOnlineQr(url, null, "pl");
        QrCodeData resultEn = qrCodeBuilder.buildOnlineQr(url, null, "en");

        assertNotNull(resultPl);
        assertNotNull(resultPl.getVerificationLinkTitle());
        assertNotNull(resultEn);
        assertNotNull(resultEn.getVerificationLinkTitle());
        assertNotEquals(resultPl.getVerificationLinkTitle(), resultEn.getVerificationLinkTitle());
    }

    @Test
    void buildCertificateQr_fromUrl_shouldGenerateQrCode() {
        String url = "https://qr-test.ksef.mf.gov.pl/certificate/Nip/6891152920/6891152920/01F20A5D352AE590/abc123/signature";

        QrCodeData result = qrCodeBuilder.buildCertificateQr(url, "pl");

        assertNotNull(result);
        assertEquals(url, result.getVerificationLink());
        assertNotNull(result.getLabel());
        assertNotNull(result.getVerificationLinkTitle());
        assertNotNull(result.getQrCodeImage());
        assertTrue(result.getQrCodeImage().length > 0);
    }
    
    @Test
    void buildCertificateQr_fromUrl_shouldTrimUrl() {
        String url = "  https://qr-test.ksef.mf.gov.pl/certificate/test  ";
        String expectedUrl = "https://qr-test.ksef.mf.gov.pl/certificate/test";

        QrCodeData result = qrCodeBuilder.buildCertificateQr(url, "pl");

        assertNotNull(result);
        assertEquals(expectedUrl, result.getVerificationLink());
    }

    @Test
    void buildCertificateQr_fromUrl_shouldUseCorrectLanguage() {
        String url = "https://qr-test.ksef.mf.gov.pl/certificate/test";

        QrCodeData resultPl = qrCodeBuilder.buildCertificateQr(url, "pl");
        QrCodeData resultEn = qrCodeBuilder.buildCertificateQr(url, "en");

        assertNotNull(resultPl);
        assertNotNull(resultPl.getVerificationLinkTitle());
        assertNotNull(resultEn);
        assertNotNull(resultEn.getVerificationLinkTitle());
        assertNotEquals(resultPl.getVerificationLinkTitle(), resultEn.getVerificationLinkTitle());
    }

    @Test
    void buildQrCodes_withDirectOnlineUrl_shouldGenerateOnlineQrCode() {
        String onlineUrl = "https://qr-test.ksef.mf.gov.pl/invoice/6891152920/08-10-2025/abc123";
        String ksefNumber = "6891152920-20251008-010000B4CF64-9C";

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .invoiceQRCodeGeneratorRequest(InvoiceQRCodeGeneratorRequest.onlineQrBuilder(onlineUrl))
                .ksefNumber(ksefNumber)
                .build();

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(onlineUrl, result.get(0).getVerificationLink());
        assertEquals(ksefNumber, result.get(0).getLabel());
    }


    @Test
    void buildQrCodes_withBothDirectUrls_shouldGenerateBothQrCodes() {
        String onlineUrl = "https://qr-test.ksef.mf.gov.pl/invoice/test";
        String certificateUrl = "https://qr-test.ksef.mf.gov.pl/certificate/test";

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .invoiceQRCodeGeneratorRequest(InvoiceQRCodeGeneratorRequest.offlineCertificateQrBuilder(onlineUrl, certificateUrl))
                .build();

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(onlineUrl, result.get(0).getVerificationLink());
        assertEquals(certificateUrl, result.get(1).getVerificationLink());
    }

    @Test
    void buildQrCodes_withOnlineUrlAndRequest_shouldUseUrlForOnlineAndRequestForCertificate() {
        String onlineUrl = "https://qr-test.ksef.mf.gov.pl/invoice/test";
        CertificateBuilders.X500NameHolder x500 = new CertificateBuilders()
                .buildForOrganization("Test Org", "VATPL-1234567890", "TestCN", "PL");
        TestCertificateGenerator generator = new TestCertificateGenerator();
        SelfSignedCertificate cert = generator.generateSelfSignedCertificateEcdsa(x500);

        InvoiceQRCodeGeneratorRequest request = InvoiceQRCodeGeneratorRequest.offlineCertificateQrBuilder(
                "https://qr-test.ksef.mf.gov.pl",
                ContextIdentifierType.NIP,
                "6891152920",
                "6891152920",
                "01F20A5D352AE590",
                cert.getPrivateKey(),
                LocalDate.of(2025, 10, 8));
        request.setOnlineQrCodeUrl(onlineUrl);

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .invoiceQRCodeGeneratorRequest(request)
                .build();

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(onlineUrl, result.get(0).getVerificationLink());
        assertTrue(result.get(1).getVerificationLink().contains("/certificate/"));
    }

    @Test
    void buildQrCodes_withRequestAndCertificateUrl_shouldUseRequestForOnlineAndUrlForCertificate() {
        String certificateUrl = "https://qr-test.ksef.mf.gov.pl/certificate/test";
        InvoiceQRCodeGeneratorRequest request = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(
                "https://qr-test.ksef.mf.gov.pl", "6891152920", LocalDate.of(2025, 10, 8));
        request.setCertificateQrCodeUrl(certificateUrl);
        request.setOnline(false);

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .invoiceQRCodeGeneratorRequest(request)
                .build();

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getVerificationLink().contains("/invoice/"));
        assertEquals(certificateUrl, result.get(1).getVerificationLink());
    }

    @Test
    void buildQrCodes_withRequestOnly_shouldGenerateFromRequest() {
        InvoiceQRCodeGeneratorRequest request = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(
                "https://qr-test.ksef.mf.gov.pl", "6891152920", LocalDate.of(2025, 10, 8));

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .invoiceQRCodeGeneratorRequest(request)
                .build();

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getVerificationLink().contains("https://qr-test.ksef.mf.gov.pl"));
    }

    @Test
    void buildQrCodes_withNoUrlsAndNoRequest_shouldReturnNull() {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .build();

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), testInvoiceXml, "pl");

        assertNull(result);
    }

    @Test
    void buildQrCodes_withBlankUrls_shouldFallbackToRequest() {
        InvoiceQRCodeGeneratorRequest request = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(
                "https://qr-test.ksef.mf.gov.pl", "6891152920", LocalDate.of(2025, 10, 8));
        request.setOnlineQrCodeUrl("   ");
        request.setCertificateQrCodeUrl("   ");

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .invoiceQRCodeGeneratorRequest(request)
                .build();

        List<QrCodeData> result = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), testInvoiceXml, "pl");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getVerificationLink().contains("https://qr-test.ksef.mf.gov.pl"));
    }

}
