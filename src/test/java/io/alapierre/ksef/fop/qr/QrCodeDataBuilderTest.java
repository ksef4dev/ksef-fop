package io.alapierre.ksef.fop.qr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that {@link QrCodeData} is populated correctly through both paths: the fluent builder
 * (whose not-null validation is deferred to {@link QrCodeData#build()}) and the plain setters
 * (which validate eagerly).
 */
class QrCodeDataBuilderTest {

    @Test
    void setsEveryProperty() {
        byte[] image = {1, 2, 3};

        QrCodeData data = QrCodeData.builder()
                .qrCodeImage(image)
                .label("KSeF 123")
                .verificationLink("https://example.test/verify")
                .verificationLinkTitle("Verify")
                .build();

        assertArrayEquals(image, data.getQrCodeImage());
        assertEquals("KSeF 123", data.getLabel());
        assertEquals("https://example.test/verify", data.getVerificationLink());
        assertEquals("Verify", data.getVerificationLinkTitle());
    }

    @Test
    void buildRejectsNullQrCodeImage() {
        QrCodeData.QrCodeDataBuilder builder = QrCodeData.builder()
                .label("label")
                .verificationLink("link")
                .verificationLinkTitle("title");

        NullPointerException ex = assertThrows(NullPointerException.class, builder::build);
        assertEquals("qrCodeImage", ex.getMessage());
    }

    @Test
    void buildRejectsNullLabel() {
        QrCodeData.QrCodeDataBuilder builder = QrCodeData.builder()
                .qrCodeImage(new byte[0])
                .verificationLink("link")
                .verificationLinkTitle("title");

        NullPointerException ex = assertThrows(NullPointerException.class, builder::build);
        assertEquals("label", ex.getMessage());
    }

    @Test
    void buildRejectsNullVerificationLink() {
        QrCodeData.QrCodeDataBuilder builder = QrCodeData.builder()
                .qrCodeImage(new byte[0])
                .label("label")
                .verificationLinkTitle("title");

        NullPointerException ex = assertThrows(NullPointerException.class, builder::build);
        assertEquals("verificationLink", ex.getMessage());
    }

    @Test
    void buildRejectsNullVerificationLinkTitle() {
        QrCodeData.QrCodeDataBuilder builder = QrCodeData.builder()
                .qrCodeImage(new byte[0])
                .label("label")
                .verificationLink("link");

        NullPointerException ex = assertThrows(NullPointerException.class, builder::build);
        assertEquals("verificationLinkTitle", ex.getMessage());
    }

    @Test
    void builderSettersDoNotValidateEagerly() {
        // Validation lives in the private constructor, so the setter accepts a null without throwing.
        assertDoesNotThrow(() -> QrCodeData.builder().label(null));
    }

    @Test
    @SuppressWarnings("deprecation")
    void settersSetEveryProperty() {
        byte[] image = {4, 5, 6};

        QrCodeData data = new QrCodeData();
        data.setQrCodeImage(image);
        data.setLabel("KSeF 999");
        data.setVerificationLink("https://example.test/verify2");
        data.setVerificationLinkTitle("Verify2");

        assertArrayEquals(image, data.getQrCodeImage());
        assertEquals("KSeF 999", data.getLabel());
        assertEquals("https://example.test/verify2", data.getVerificationLink());
        assertEquals("Verify2", data.getVerificationLinkTitle());
    }

    @Test
    @SuppressWarnings("deprecation")
    void settersRejectNull() {
        QrCodeData data = new QrCodeData();

        assertEquals("qrCodeImage",
                assertThrows(NullPointerException.class, () -> data.setQrCodeImage(null)).getMessage());
        assertEquals("label",
                assertThrows(NullPointerException.class, () -> data.setLabel(null)).getMessage());
        assertEquals("verificationLink",
                assertThrows(NullPointerException.class, () -> data.setVerificationLink(null)).getMessage());
        assertEquals("verificationLinkTitle",
                assertThrows(NullPointerException.class, () -> data.setVerificationLinkTitle(null)).getMessage());
    }
}