package io.alapierre.ksef.fop;

import io.alapierre.ksef.fop.qr.QrCodeData;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

/**
 * Verifies the {@code equals}/{@code hashCode} contract of the value classes that carry hand-written
 * implementations. Two warnings are suppressed because of how these classes are shaped, not to relax
 * the relation itself: {@code NONFINAL_FIELDS} (they are mutable JavaBeans exposing setters) and
 * {@code STRICT_INHERITANCE} (they are non-final and use the {@code canEqual} pattern instead of a
 * final {@code equals}).
 */
class EqualsContractTest {

    @Test
    void invoiceGenerationParams() {
        EqualsVerifier.forClass(InvoiceGenerationParams.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    void upoGenerationParams() {
        EqualsVerifier.forClass(UpoGenerationParams.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    void qrCodeData() {
        EqualsVerifier.forClass(QrCodeData.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }
}