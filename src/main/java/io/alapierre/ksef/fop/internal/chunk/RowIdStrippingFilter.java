package io.alapierre.ksef.fop.internal.chunk;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Removes the row ids added by {@link FoSpooler}, so a single-pass render of the spooled FO gets exactly the
 * document produced by the template.
 */
final class RowIdStrippingFilter extends XMLFilterImpl {

    RowIdStrippingFilter(XMLReader parent) {
        super(parent);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        int id = attributes.getIndex("", "id");
        if (id >= 0 && attributes.getValue(id).startsWith(FoNames.ROW_ID_PREFIX)) {
            AttributesImpl withoutId = new AttributesImpl(attributes);
            withoutId.removeAttribute(id);
            super.startElement(uri, localName, qName, withoutId);
        } else {
            super.startElement(uri, localName, qName, attributes);
        }
    }
}
