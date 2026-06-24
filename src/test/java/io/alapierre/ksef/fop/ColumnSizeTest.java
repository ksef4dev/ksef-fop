package io.alapierre.ksef.fop;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Visual tests for invoice row column sizing.
 *
 * <p><strong>Warning:</strong> These tests don't have assertions and need to be checked manually to see if the output
 * in {@code target/test-output/faktury/ColumnSizeTest} looks OK.
 */
class ColumnSizeTest extends AbstractGeneratePdfTest {

    @ParameterizedTest
    @ValueSource(strings = {"GTIN", "PKWiU", "CN", "PKOB"})
    void columnFitsContent(String column) throws Exception {
        byte[] pdfData = generateFa3InvoicePdf("faktury/ColumnSizeTest/" + column + ".xml");
        writeDebugData(pdfData, "faktury/ColumnSizeTest/" + column);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GTIN", "PKWiU", "CN", "PKOB"})
    void diffColumnFitsContent(String column) throws Exception {
        byte[] pdfData = generateFa3InvoicePdf("faktury/ColumnSizeTest/diff_" + column + ".xml");
        writeDebugData(pdfData, "faktury/ColumnSizeTest/diff_" + column);
    }
}