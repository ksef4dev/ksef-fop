package io.alapierre.ksef.fop;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Node;
import org.xmlunit.assertj3.XmlAssert;

import java.net.URL;

/**
 * Tests the transformation of the {@code crd:Adnotacje} element.
 */
class AdnotacjeTemplateTest extends AbstractStyleSheetTest {

    @ParameterizedTest
    @ValueSource(strings = {"minimal", "exemption_a", "exemption_b", "exemption_c", "exemption_abc", "exemption_p19_only", "simplified"})
    void testTemplate(String resourceName) throws Exception {
        URL inputUrl = resource("AdnotacjeTemplateTest/" + resourceName + ".xml");
        URL expectedUrl = resource("AdnotacjeTemplateTest/" + resourceName + "-expected.xml");

        Node actual = transformFa3Invoice(inputUrl,  "/fa3:Faktura/fa3:Fa/fa3:Adnotacje", null);

        XmlAssert.assertThat(actual)
                .and(expectedUrl)
                .ignoreWhitespace()
                .areSimilar();
    }

}
