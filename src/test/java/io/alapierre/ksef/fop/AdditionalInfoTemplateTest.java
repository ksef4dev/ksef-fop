package io.alapierre.ksef.fop;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests the visualization of the "Additional information" invoice section.
 */
class AdditionalInfoTemplateTest extends AbstractStyleSheetTest {

    private static final String INVOICE_XPATH = "/fa3:Faktura";
    private static final String TP_XPATH = "//fo:block[@id='TP']";

    @Test
    void shouldDisplayTpAnnotationWhenPresent() throws Exception {
        URL input = resource("AdditionalInfoTemplateTest/tp_present.xml");

        Node tp = transformFa3Invoice(input, INVOICE_XPATH, TP_XPATH);

        assertNotNull(tp, "TP annotation should be visible when present in xml");
        assertFalse(tp.getTextContent().trim().isEmpty(), "TP annotation should display a non-empty text");
    }

    @Test
    void shouldNotDisplayTpAnnotationWhenAbsent() throws Exception {
        URL input = resource("AdditionalInfoTemplateTest/tp_absent.xml");

        Node tp = transformFa3Invoice(input, INVOICE_XPATH, TP_XPATH);

        assertNull(tp, "TP annotation should not be visible when absent in xml");
    }
}
