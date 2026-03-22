package io.alapierre.ksef.fop;

import io.alapierre.ksef.fop.i18n.TranslationService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.assertj3.XmlAssert;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.net.URL;

class AdnotacjeTemplateTest {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static final String FA3_TEMPLATE_RESOURCE = "templates/fa3/ksef_invoice.xsl";
    private static final URL FA3_TEMPLATE_URL = resource(FA3_TEMPLATE_RESOURCE);

    private static final TranslationService translationService = new TranslationService();

    static {
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
    }

    private static Document transformFa3Adnotacje(URL input) throws Exception {
        try (InputStream xsl = FA3_TEMPLATE_URL.openStream();
             InputStream xml = input.openStream()) {
            // Retrieve the `Adnotacje` element
            DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            Document sourceDocument = documentBuilder.parse(xml, input.toExternalForm());
            Node adnotacje = sourceDocument.getElementsByTagName("Adnotacje").item(0);
            // Setup transformer
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xsl, FA3_TEMPLATE_URL.toExternalForm()));
            transformer.setParameter("labels", translationService.getTranslationsAsXml(null));
            // Create root element to contain the result
            Document resultDocument = documentBuilder.newDocument();
            Element rootElement = resultDocument.createElementNS(null, "root");
            resultDocument.appendChild(rootElement);
            // Run the transformation
            Source xmlSource = new DOMSource(adnotacje);
            DOMResult domResult = new DOMResult(resultDocument.getDocumentElement());
            transformer.transform(xmlSource, domResult);
            return resultDocument;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"minimal", "exemption_a", "exemption_b", "exemption_c", "simplified"})
    void testTemplate(String resourceName) throws Exception {
        URL inputUrl = resource("AdnotacjeTemplateTest/" + resourceName + ".xml");
        URL expectedUrl = resource("AdnotacjeTemplateTest/" + resourceName + "-expected.xml");

        Document actual = transformFa3Adnotacje(inputUrl);

        XmlAssert.assertThat(actual)
                .and(expectedUrl)
                .ignoreWhitespace()
                .areSimilar();
    }

    private static URL resource(String resourceName) {
        return AdnotacjeTemplateTest.class.getClassLoader().getResource(resourceName);
    }
}
