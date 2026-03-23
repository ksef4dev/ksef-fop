package io.alapierre.ksef.fop;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Node;
import org.xmlunit.assertj3.XmlAssert;

import java.net.URL;

/**
 * Tests the visualization of numeric values.
 */
class NumericFormatTest extends AbstractStyleSheetTest {

    private static final URL FA3_ROW_TEMPLATE = resource("templates/fa3/invoice-rows.xsl");


    @ParameterizedTest
    @ValueSource(strings = {
            "tax_summary"
    })
    void testTaxSummary(String resourceName) throws Exception {
        URL inputUrl = resource("NumericFormatTest/" + resourceName + ".xml");
        URL expectedUrl = resource("NumericFormatTest/" + resourceName + "-expected.xml");

        Node actual = transformFa3Invoice(inputUrl, "/fa3:Faktura", "//fo:table[@id='tax_summary']");

        XmlAssert.assertThat(actual)
                .and(expectedUrl)
                .ignoreWhitespace()
                .areSimilar();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "row",
            "row_max_precision"
    })
    void testInvoiceRow(String resourceName) throws Exception {
        URL inputUrl = resource("NumericFormatTest/" + resourceName + ".xml");
        URL expectedUrl = resource("NumericFormatTest/" + resourceName + "-expected.xml");

        Node actual = transformFa3Invoice(inputUrl, "/fa3:Faktura/fa3:Fa/fa3:FaWiersz", null);

        XmlAssert.assertThat(actual)
                .and(expectedUrl)
                .ignoreWhitespace()
                .areSimilar();
    }

    private static Node transformFa3Row(URL inputUrl, String inputXpath, String outputXpath) throws Exception {
        return transformXml(inputUrl, FA3_ROW_TEMPLATE, inputXpath, outputXpath, AbstractStyleSheetTest::setLabelsParam);
    }
}
