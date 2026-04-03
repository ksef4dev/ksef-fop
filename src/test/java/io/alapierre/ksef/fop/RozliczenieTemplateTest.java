package io.alapierre.ksef.fop;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Node;
import org.xmlunit.assertj3.XmlAssert;

import java.net.URL;

/**
 * Tests the transformation of the {@code crd:Rozliczenie} element.
 */
class RozliczenieTemplateTest extends AbstractStyleSheetTest {

    @ParameterizedTest
    @ValueSource(strings = {"do_zaplaty", "do_rozliczenia", "obciazenia", "odliczenia"})
    void testTemplate(String resourceName) throws Exception {
        URL inputUrl = resource("RozliczenieTemplateTest/" + resourceName + ".xml");
        URL expectedUrl = resource("RozliczenieTemplateTest/" + resourceName + "-expected.xml");

        Node actual = transformFa3Invoice(inputUrl, "/fa3:Faktura", "//fo:block[@id='Rozliczenie']");

        XmlAssert.assertThat(actual)
                .and(expectedUrl)
                .ignoreWhitespace()
                .areSimilar();
    }
}