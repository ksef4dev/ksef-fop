package io.alapierre.ksef.fop.internal.chunk;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Passes an intermediate-format document through, dropping every page after the first {@code pagesToKeep}.
 */
final class PageDroppingFilter extends XMLFilterImpl {

    private final int pagesToKeep;
    private int pagesSeen;
    private int droppedDepth;

    PageDroppingFilter(XMLReader parent, int pagesToKeep) {
        super(parent);
        this.pagesToKeep = pagesToKeep;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (isDropping()) {
            droppedDepth++;
        } else if (IntermediateFormat.isElement(uri, localName, IntermediateFormat.PAGE) && ++pagesSeen > pagesToKeep) {
            droppedDepth = 1;
        } else {
            super.startElement(uri, localName, qName, attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isDropping()) {
            droppedDepth--;
        } else {
            super.endElement(uri, localName, qName);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (!isDropping()) {
            super.characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (!isDropping()) {
            super.ignorableWhitespace(ch, start, length);
        }
    }

    private boolean isDropping() {
        return droppedDepth > 0;
    }
}
