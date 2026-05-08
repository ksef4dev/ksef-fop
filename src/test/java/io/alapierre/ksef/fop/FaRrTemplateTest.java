package io.alapierre.ksef.fop;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class FaRrTemplateTest extends AbstractStyleSheetTest {

    private static final URL FA_RR_TEMPLATE_URL = resource("templates/fa_rr/ksef_invoice.xsl");
    private static final URL FA_RR_INPUT_URL = resource("faktury/fa_rr/podstawowa/FA_RR_1_Przyklad_1.xml");

    @Test
    void transformsFaRrInvoice() throws Exception {
        Node actual = transformXml(FA_RR_INPUT_URL, FA_RR_TEMPLATE_URL, "/*", "//fo:root", AbstractStyleSheetTest::setLabelsParam);

        assertThat(actual).isNotNull();
    }
}
