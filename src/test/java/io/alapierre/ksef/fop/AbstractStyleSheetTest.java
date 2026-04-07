package io.alapierre.ksef.fop;

import io.alapierre.ksef.fop.i18n.TranslationService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Generalized helper for stylesheet-based tests.
 */
public abstract class AbstractStyleSheetTest {

    private static final String FA3_NS = "http://crd.gov.pl/wzor/2025/06/25/13775/";
    private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

    protected static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    protected static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    protected static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

    private static final String FA3_TEMPLATE_RESOURCE = "templates/fa3/ksef_invoice.xsl";
    private static final URL FA3_TEMPLATE_URL = AbstractStyleSheetTest.resource(FA3_TEMPLATE_RESOURCE);

    private static final TranslationService translationService = new TranslationService();

    static {
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
    }

    protected static URL resource(String resourceName) {
        return AbstractStyleSheetTest.class.getClassLoader().getResource(resourceName);
    }

    private static NamespaceContext createNamespaceContext() {
        // This context is used by XPath to allow for shorter expressions
        return new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "fa3":
                        return FA3_NS;
                    case "fo":
                        return FO_NS;
                    case "xml":
                        return XMLConstants.XML_NS_URI;
                }
                return XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                // unused
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                // unused
                return Collections.emptyIterator();
            }
        };
    }

    /**
     * Transform an XML document using an XSLT stylesheet and return the result as a new DOM
     * Document.
     *
     * <p>Steps performed:</p>
     * <ol>
     *   <li>Reads the input XML from {@code inputUrl}.</li>
     *   <li>Evaluates {@code inputXpath} against the parsed document and selects a single node.</li>
     *   <li>Reads the XSLT stylesheet from {@code xslUrl}.</li>
     *   <li>Applies the stylesheet to the selected node.</li>
     *   <li>Returns the {@link Node} corresponding to {@code outputXpath} or the transformed {@link Document}
     *   if {@code outputXpath} is {@code null}.</li>
     * </ol>
     *
     * <p>Both input streams are closed before the method returns. If the XPath expression
     * does not select a node an {@link IllegalArgumentException} is thrown.</p>
     *
     * @param inputUrl              the URL of the input XML document
     * @param xslUrl                the URL of the XSLT stylesheet
     * @param inputXpath            an XPath expression used to select the node to transform
     * @param outputXpath           an XPath expression used to select the node in the result returned or {@code null}
     * @param transformerConfigurer a consumer that can be used to configure the {@link Transformer}, can be {@code null}
     * @return a new {@link Document} containing the transformation result
     * @throws Exception on parsing, XPath evaluation or transformation errors
     */
    protected static Node transformXml(URL inputUrl, URL xslUrl, String inputXpath, String outputXpath, Consumer<Transformer> transformerConfigurer) throws Exception {
        try (InputStream xml = inputUrl.openStream(); InputStream xsl = xslUrl.openStream()) {
            DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            Document sourceDocument = documentBuilder.parse(xml, inputUrl.toExternalForm());

            XPath xpath = XPATH_FACTORY.newXPath();
            xpath.setNamespaceContext(createNamespaceContext());

            Node selectedNode = (Node) xpath.evaluate(inputXpath, sourceDocument, XPathConstants.NODE);
            if (selectedNode == null) {
                throw new IllegalArgumentException("XPath expression did not select any node: " + inputXpath);
            }

            Transformer transformer = TRANSFORMER_FACTORY.newTransformer(new StreamSource(xsl, xslUrl.toExternalForm()));
            if (transformerConfigurer != null) {
                transformerConfigurer.accept(transformer);
            }

            Document resultDocument = documentBuilder.newDocument();
            Node root = resultDocument.createElementNS(null, "root");
            resultDocument.appendChild(root);

            DOMResult domResult = new DOMResult(resultDocument.getDocumentElement());
            transformer.transform(new DOMSource(selectedNode), domResult);

            if (outputXpath != null) {
                // ensure namespace context is present for evaluating the output XPath
                xpath.setNamespaceContext(createNamespaceContext());
                return (Node) xpath.evaluate(outputXpath, resultDocument, XPathConstants.NODE);
            }

            return resultDocument;
        }
    }

    protected static void setLabelsParam(Transformer transformer) {
        transformer.setParameter("labels", translationService.getTranslationsAsXml(null));
    }

    /**
     * Transforms an XML document using the FA(3) stylesheet
     *
     * <p>This method also provides all the parameters required by the stylesheet.</p>
     *
     * @param inputUrl    the URL of the input XML document
     * @param inputXpath  an XPath expression used to select the node to transform
     * @param outputXpath an XPath expression used to select the node in the result returned or {@code null}
     * @return a new {@link Document} containing the transformation result
     * @throws Exception on parsing, XPath evaluation or transformation errors
     */
    protected static Node transformFa3Invoice(URL inputUrl, String inputXpath, String outputXpath) throws Exception {
        return transformXml(inputUrl, FA3_TEMPLATE_URL, inputXpath, outputXpath, AbstractStyleSheetTest::setLabelsParam);
    }
}

