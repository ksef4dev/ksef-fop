/*
 * SPDX-License-identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

class XmlFactoriesTest {

    private static final String EXTERNAL_ENTITY_XML = "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE foo [\n" +
            "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\">\n" +
            "]>\n" +
            "<root>&xxe;</root>";

    private static final String TRIVIAL_STYLESHEET = "<?xml version=\"1.0\"?>\n" +
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
            "  <xsl:template match=\"/\">\n" +
            "    <result/>\n" +
            "  </xsl:template>\n" +
            "</xsl:stylesheet>";

    private static final String BILLION_LAUGHS_XML = "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE lolz [\n" +
            "  <!ENTITY lol \"lol\">\n" +
            "  <!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
            "  <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
            "  <!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
            "  <!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
            "  <!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
            "  <!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
            "  <!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
            "  <!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
            "]>\n" +
            "<root>&lol9;</root>";

    private static final OutputStream DEV_NULL = new OutputStream() {
        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }
    };

    /**
     * Verifies that a billion-laughs XML payload is rejected.
     */
    @Test
    void documentBuilderRejectsBillionLaughs() throws Exception {
        DocumentBuilder builder = XmlFactories.DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(BILLION_LAUGHS_XML.getBytes(StandardCharsets.UTF_8));

        assertThrows(SAXException.class, () -> builder.parse(input));
    }

    /**
     * Verifies that an XXE payload attempting to read a local file is rejected.
     */
    @Test
    void documentBuilderRejectsExternalEntity() throws Exception {
        DocumentBuilder builder = XmlFactories.DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(EXTERNAL_ENTITY_XML.getBytes(StandardCharsets.UTF_8));

        assertThrows(SAXException.class, () -> builder.parse(input));
    }

    /**
     * Verifies that a billion-laughs XML payload is rejected.
     */
    @Test
    void transformerRejectsBillionLaughs() throws Exception {
        Transformer transformer = XmlFactories.createTransformerFactory().newTransformer();
        StreamSource source = new StreamSource(new ByteArrayInputStream(BILLION_LAUGHS_XML.getBytes(StandardCharsets.UTF_8)));

        assertThrows(TransformerException.class,
                () -> transformer.transform(source, new StreamResult(DEV_NULL)));
    }

    /**
     * Verifies that an XXE payload attempting to read a local file is rejected.
     */
    @Test
    void transformerRejectsExternalEntity() throws Exception {
        Transformer transformer = XmlFactories.createTransformerFactory().newTransformer();
        StreamSource source = new StreamSource(new ByteArrayInputStream(EXTERNAL_ENTITY_XML.getBytes(StandardCharsets.UTF_8)));

        assertThrows(TransformerException.class,
                () -> transformer.transform(source, new StreamResult(DEV_NULL)));
    }

    /**
     * Verifies that a billion-laughs XML payload is rejected by a stylesheet-driven transformer.
     */
    @Test
    void stylesheetTransformerRejectsBillionLaughs() throws Exception {
        Transformer transformer = createStylesheetTransformer();
        StreamSource source = new StreamSource(new ByteArrayInputStream(BILLION_LAUGHS_XML.getBytes(StandardCharsets.UTF_8)));

        assertThrows(TransformerException.class,
                () -> transformer.transform(source, new StreamResult(DEV_NULL)));
    }

    /**
     * Verifies that an XXE payload is rejected by a stylesheet-driven transformer.
     */
    @Test
    void stylesheetTransformerRejectsExternalEntity() throws Exception {
        Transformer transformer = createStylesheetTransformer();
        StreamSource source = new StreamSource(new ByteArrayInputStream(EXTERNAL_ENTITY_XML.getBytes(StandardCharsets.UTF_8)));

        assertThrows(TransformerException.class,
                () -> transformer.transform(source, new StreamResult(DEV_NULL)));
    }

    private static Transformer createStylesheetTransformer() throws TransformerException {
        StreamSource stylesheet = new StreamSource(new ByteArrayInputStream(TRIVIAL_STYLESHEET.getBytes(StandardCharsets.UTF_8)));
        return XmlFactories.createTransformerFactory().newTransformer(stylesheet);
    }
}
