package io.alapierre.ksef.fop.qr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that each {@link QrCodeData.QrCodeDataBuilder} method sets the matching property on the
 * built instance, and that the not-null validation is enforced by {@link QrCodeData#build()} rather
 * than by the individual builder setters.
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
}