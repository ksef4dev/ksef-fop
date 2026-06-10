package io.alapierre.ksef.fop;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that both construction paths populate {@link InvoiceGenerationParams} and
 * {@link UpoGenerationParams}: the fluent builder (which deliberately does not follow JavaBean
 * getter/setter naming) and the plain setters on a no-arg instance.
 */
class GenerationParamsBuilderTest {

    private static final Path ROOT_A = Paths.get("/templates/a");
    private static final Path ROOT_B = Paths.get("/templates/b");

    @Nested
    class InvoiceParams {

        @Test
        @SuppressWarnings("deprecation")
        void setsEveryProperty() {
            byte[] logo = {1, 2, 3};
            URI logoUri = URI.create("https://example.test/logo.png");
            LocalDate currencyDate = LocalDate.of(2026, 6, 10);
            InvoiceQRCodeGeneratorRequest qr = InvoiceQRCodeGeneratorRequest.onlineQrBuilder("https://example.test/qr");
            Map<String, Object> customProperties = new HashMap<>();
            customProperties.put("foo", "bar");

            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .verificationLink("https://example.test/verify")
                    .logo(logo)
                    .logoUri(logoUri)
                    .currencyDate(currencyDate)
                    .issuerUser("issuer")
                    .showCorrectionDifferences(true)
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .ksefNumber("KSEF-123")
                    .invoiceQRCodeGeneratorRequest(qr)
                    .templatePath("/custom/template.xsl")
                    .customProperties(customProperties)
                    .language(Language.EN)
                    .languageLocale("en-US")
                    .build();

            assertEquals("https://example.test/verify", params.getVerificationLink());
            assertArrayEquals(logo, params.getLogo());
            assertEquals(logoUri, params.getLogoUri());
            assertEquals(currencyDate, params.getCurrencyDate());
            assertEquals("issuer", params.getIssuerUser());
            assertTrue(params.isShowCorrectionDifferences());
            assertEquals(InvoiceSchema.FA3_1_0_E, params.getSchema());
            assertEquals("KSEF-123", params.getKsefNumber());
            assertSame(qr, params.getInvoiceQRCodeGeneratorRequest());
            assertEquals("/custom/template.xsl", params.getTemplatePath());
            assertEquals(customProperties, params.getCustomProperties());
            assertEquals(Language.EN, params.getLanguage());
            assertEquals("en-US", params.getLanguageLocale());
        }

        @Test
        @SuppressWarnings("deprecation")
        void settersSetEveryProperty() {
            byte[] logo = {4, 5, 6};
            URI logoUri = URI.create("https://example.test/logo2.png");
            LocalDate currencyDate = LocalDate.of(2026, 1, 2);
            InvoiceQRCodeGeneratorRequest qr = InvoiceQRCodeGeneratorRequest.onlineQrBuilder("https://example.test/qr2");
            Map<String, Object> customProperties = new HashMap<>();
            customProperties.put("baz", "qux");

            InvoiceGenerationParams params = new InvoiceGenerationParams();
            params.setVerificationLink("https://example.test/verify2");
            params.setLogo(logo);
            params.setLogoUri(logoUri);
            params.setCurrencyDate(currencyDate);
            params.setIssuerUser("issuer2");
            params.setShowCorrectionDifferences(true);
            params.setSchema(InvoiceSchema.FA3_1_0_E);
            params.setKsefNumber("KSEF-999");
            params.setInvoiceQRCodeGeneratorRequest(qr);
            params.setTemplatePath("/custom/template2.xsl");
            params.setCustomProperties(customProperties);
            params.setLanguage(Language.EN);
            params.setLanguageLocale("uk");

            assertEquals("https://example.test/verify2", params.getVerificationLink());
            assertArrayEquals(logo, params.getLogo());
            assertEquals(logoUri, params.getLogoUri());
            assertEquals(currencyDate, params.getCurrencyDate());
            assertEquals("issuer2", params.getIssuerUser());
            assertTrue(params.isShowCorrectionDifferences());
            assertEquals(InvoiceSchema.FA3_1_0_E, params.getSchema());
            assertEquals("KSEF-999", params.getKsefNumber());
            assertSame(qr, params.getInvoiceQRCodeGeneratorRequest());
            assertEquals("/custom/template2.xsl", params.getTemplatePath());
            assertEquals(customProperties, params.getCustomProperties());
            assertEquals(Language.EN, params.getLanguage());
            assertEquals("uk", params.getLanguageLocale());
        }

        @Test
        void templateRootAppendsInOrder() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .templateRoot(ROOT_A)
                    .templateRoot(ROOT_B)
                    .build();

            assertEquals(Arrays.asList(ROOT_A, ROOT_B), params.getTemplateRoots());
        }

        @Test
        void templateRootsAddsWholeCollection() {
            List<Path> roots = Arrays.asList(ROOT_A, ROOT_B);

            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .templateRoots(roots)
                    .build();

            assertEquals(roots, params.getTemplateRoots());
        }

        @Test
        void templateRootsToleratesNull() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .templateRoots(null)
                    .build();

            assertTrue(params.getTemplateRoots().isEmpty());
        }

        @Test
        void clearTemplateRootsRemovesPreviouslyAddedRoots() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .templateRoot(ROOT_A)
                    .clearTemplateRoots()
                    .templateRoot(ROOT_B)
                    .build();

            assertEquals(Collections.singletonList(ROOT_B), params.getTemplateRoots());
        }

        @Test
        void templateRootsViewIsUnmodifiable() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .templateRoot(ROOT_A)
                    .build();

            List<Path> roots = params.getTemplateRoots();
            assertThrows(UnsupportedOperationException.class, () -> roots.add(ROOT_B));
        }
    }

    @Nested
    class UpoParams {

        @Test
        @SuppressWarnings("deprecation")
        void setsEveryProperty() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .language(Language.EN)
                    .languageLocale("en-US")
                    .templatePath("/custom/upo.xsl")
                    .build();

            assertEquals(UpoSchema.UPO_V4_3, params.getSchema());
            assertEquals(Language.EN, params.getLanguage());
            assertEquals("en-US", params.getLanguageLocale());
            assertEquals("/custom/upo.xsl", params.getTemplatePath());
        }

        @Test
        @SuppressWarnings("deprecation")
        void settersSetEveryProperty() {
            UpoGenerationParams params = new UpoGenerationParams();
            params.setSchema(UpoSchema.UPO_V4_3);
            params.setLanguage(Language.EN);
            params.setLanguageLocale("uk");
            params.setTemplatePath("/custom/upo2.xsl");

            assertEquals(UpoSchema.UPO_V4_3, params.getSchema());
            assertEquals(Language.EN, params.getLanguage());
            assertEquals("uk", params.getLanguageLocale());
            assertEquals("/custom/upo2.xsl", params.getTemplatePath());
        }

        @Test
        void templateRootAppendsInOrder() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .templateRoot(ROOT_A)
                    .templateRoot(ROOT_B)
                    .build();

            assertEquals(Arrays.asList(ROOT_A, ROOT_B), params.getTemplateRoots());
        }

        @Test
        void templateRootsAddsWholeCollection() {
            List<Path> roots = Arrays.asList(ROOT_A, ROOT_B);

            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .templateRoots(roots)
                    .build();

            assertEquals(roots, params.getTemplateRoots());
        }

        @Test
        void templateRootsToleratesNull() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .templateRoots(null)
                    .build();

            assertTrue(params.getTemplateRoots().isEmpty());
        }

        @Test
        void clearTemplateRootsRemovesPreviouslyAddedRoots() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .templateRoot(ROOT_A)
                    .clearTemplateRoots()
                    .templateRoot(ROOT_B)
                    .build();

            assertEquals(Collections.singletonList(ROOT_B), params.getTemplateRoots());
        }

        @Test
        void templateRootsViewIsUnmodifiable() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .templateRoot(ROOT_A)
                    .build();

            List<Path> roots = params.getTemplateRoots();
            assertThrows(UnsupportedOperationException.class, () -> roots.add(ROOT_B));
        }
    }
}