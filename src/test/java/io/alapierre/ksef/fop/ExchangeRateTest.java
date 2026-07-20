package io.alapierre.ksef.fop;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests how the currency exchange rate ({@code crd:KursWaluty}) is visualized on FA(3) invoices.
 */
class ExchangeRateTest extends AbstractStyleSheetTest {

    private static final String INVOICE_XPATH = "/fa3:Faktura";
    private static final String EXCHANGE_RATE_HEADER_XPATH = "//fo:block[@id='exchangeRate']";
    private static final String EXCHANGE_RATE_NOTE_HEADER_XPATH = "//fo:block[@id='exchangeRateCommonNote']";
    private static final String FIRST_ROW_XPATH = "(//fo:table-cell[starts-with(@id,'lineExchangeRate')])[1]";
    private static final String SECOND_ROW_XPATH = "(//fo:table-cell[starts-with(@id,'lineExchangeRate')])[2]";

    @Test
    void shouldCorrectlyDisplayDifferentLineRatesForVatInvoice() throws Exception {
        URL input = resource("ExchangeRateTest/vat_different_rates_in_rows.xml");

        String firstRowExpectedRate = "4.3200";
        String secondRowExpectedRate = "4.500";

        Node exchangeRateHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_HEADER_XPATH);
        Node exchangeRateNoteHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_NOTE_HEADER_XPATH);
        Node firstRow = transformFa3Invoice(input, INVOICE_XPATH, FIRST_ROW_XPATH);
        Node secondRow = transformFa3Invoice(input, INVOICE_XPATH, SECOND_ROW_XPATH);

        assertNull(exchangeRateNoteHeader, "Exchange rate header note should not be visible when rows have different rates");
        assertNull(exchangeRateHeader, "Exchange rate header should not be visible when rows have different rates");
        assertTrue(firstRow.getTextContent().contains(firstRowExpectedRate),
                () -> "Expected line 1 exchange rate " + firstRowExpectedRate + " but got: " + firstRow.getTextContent());
        assertTrue(secondRow.getTextContent().contains(secondRowExpectedRate),
                () -> "Expected line 2 exchange rate " + secondRowExpectedRate + " but got: " + secondRow.getTextContent());
    }

    @Test
    void shouldCorrectlyDisplaySameLineRatesForVatInvoice() throws Exception {
        URL input = resource("ExchangeRateTest/vat_same_rates_in_rows.xml");

        String firstRowExpectedRate = "4.3200";
        String commonRateExpectedNote = "Kurs waluty wspólny dla wszystkich wierszy faktury";

        Node exchangeRateHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_HEADER_XPATH);
        Node exchangeRateNoteHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_NOTE_HEADER_XPATH);
        Node firstRow = transformFa3Invoice(input, INVOICE_XPATH, FIRST_ROW_XPATH);

        assertNotNull(exchangeRateNoteHeader, "Exchange rate header should be visible when rows have equal rates");
        assertTrue(exchangeRateHeader.getTextContent().contains(firstRowExpectedRate),
                () -> "Expected common exchange rate " + firstRowExpectedRate
                        + " but got: " + exchangeRateHeader.getTextContent());

        assertTrue(exchangeRateNoteHeader.getTextContent().contains(commonRateExpectedNote),
                () -> "Expected common exchange rate note but got: " + exchangeRateNoteHeader.getTextContent());
        assertNull(firstRow, "Per-line exchange rate column must not be shown when all line rates are equal");
    }

    @Test
    void shouldCorrectlyDisplayIncompleteLineRatesForVatInvoice() throws Exception {
        URL input = resource("ExchangeRateTest/vat_incomplete_rates_in_rows.xml");

        String firstRowExpectedRate = "4.3200";

        Node exchangeRateHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_HEADER_XPATH);
        Node exchangeRateNoteHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_NOTE_HEADER_XPATH);
        Node firstRow = transformFa3Invoice(input, INVOICE_XPATH, FIRST_ROW_XPATH);

        assertNull(exchangeRateHeader,
                "Exchange rate header should not be visible when rows have incomplete rates");
        assertNull(exchangeRateNoteHeader,
                "Exchange rate header note should not be visible when rows have incomplete rates");

        assertNotNull(firstRow,
                "Exchange rate should be shown per line when rows have incomplete rates");
        assertTrue(firstRow.getTextContent().contains(firstRowExpectedRate),
                () -> "Expected line 1 exchange rate " + firstRowExpectedRate + " but got: " + firstRow.getTextContent());
    }

    @Test
    void shouldCorrectlyDisplayCorrectedExchangeRates() throws Exception {
        URL input = resource("faktury/fa3/korygujaca/FA_3_Przyklad_3.xml");

        String beforeCorrectionRate = "4.2106";
        String afterCorrectionRate = "4.2142";

        Node exchangeRateHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_HEADER_XPATH);
        Node exchangeRateNoteHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_NOTE_HEADER_XPATH);
        Node firstRow = transformFa3Invoice(input, INVOICE_XPATH, FIRST_ROW_XPATH);
        Node secondRow = transformFa3Invoice(input, INVOICE_XPATH, SECOND_ROW_XPATH);

        assertNull(exchangeRateHeader,
                "Common exchange rate should not be shown when the rate is corrected");
        assertNull(exchangeRateNoteHeader,
                "Common exchange rate note should not be shown when the rate is corrected");
        assertNotNull(firstRow,
                "Exchange rate should be shown per line for a rate correction");
        assertTrue(firstRow.getTextContent().contains(beforeCorrectionRate),
                () -> "Expected before-correction rate " + beforeCorrectionRate + " but got: " + firstRow.getTextContent());
        assertTrue(secondRow.getTextContent().contains(afterCorrectionRate),
                () -> "Expected after-correction rate " + afterCorrectionRate + " but got: " + secondRow.getTextContent());
    }

    @Test
    void shouldHideExchangeRateForCorrectionWithoutRateChange() throws Exception {
        URL input = resource("ExchangeRateTest/correction_unchanged_rate.xml");

        Node exchangeRateHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_HEADER_XPATH);
        Node exchangeRateNoteHeader = transformFa3Invoice(input, INVOICE_XPATH, EXCHANGE_RATE_NOTE_HEADER_XPATH);
        Node firstRow = transformFa3Invoice(input, INVOICE_XPATH, FIRST_ROW_XPATH);

        assertNull(exchangeRateHeader,
                "Common exchange rate header should not be shown on a correction that does not change the rate");
        assertNull(exchangeRateNoteHeader,
                "Common exchange rate note should not be shown on a correction that does not change the rate");
        assertNull(firstRow,
                "Per-line exchange rate column should not be shown on a correction that does not change the rate");
    }
}
