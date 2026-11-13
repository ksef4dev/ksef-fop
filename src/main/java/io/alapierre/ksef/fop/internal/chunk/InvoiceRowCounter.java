package io.alapierre.ksef.fop.internal.chunk;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

/**
 * Counts invoice positions ({@code Fa/FaWiersz}) with a streaming parser.
 */
final class InvoiceRowCounter {

    private static final String INVOICE_ELEMENT = "Fa";
    private static final String ROW_ELEMENT = "FaWiersz";

    private InvoiceRowCounter() {
    }

    static int countRows(byte[] invoiceXml) throws XMLStreamException {
        XMLStreamReader reader = newInputFactory().createXMLStreamReader(new ByteArrayInputStream(invoiceXml));
        try {
            return countRows(reader);
        } finally {
            reader.close();
        }
    }

    private static int countRows(XMLStreamReader reader) throws XMLStreamException {
        int depth = 0;
        int invoiceDepth = -1;
        int rows = 0;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
                if (invoiceDepth < 0 && INVOICE_ELEMENT.equals(reader.getLocalName())) {
                    invoiceDepth = depth;
                } else if (depth == invoiceDepth + 1 && ROW_ELEMENT.equals(reader.getLocalName())) {
                    rows++;
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (depth == invoiceDepth) {
                    return rows;
                }
                depth--;
            }
        }
        return rows;
    }

    private static XMLInputFactory newInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }
}
