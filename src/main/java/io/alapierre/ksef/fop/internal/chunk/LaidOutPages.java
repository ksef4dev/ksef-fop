package io.alapierre.ksef.fop.internal.chunk;

import io.alapierre.ksef.fop.internal.XmlFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.OptionalInt;

/**
 * Page layout of one chunk read from its intermediate-format file: number of pages and the first position that
 * starts on the last page.
 */
final class LaidOutPages {

    private final int pageCount;
    private final OptionalInt firstRowOnLastPage;

    private LaidOutPages(int pageCount, OptionalInt firstRowOnLastPage) {
        this.pageCount = pageCount;
        this.firstRowOnLastPage = firstRowOnLastPage;
    }

    static LaidOutPages read(File intermediateFormat) throws IOException, SAXException {
        PageHandler handler = new PageHandler();
        try {
            XmlFactories.SAX_PARSER_FACTORY.newSAXParser().parse(intermediateFormat, handler);
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
        return new LaidOutPages(handler.pageCount, handler.firstRowOnLastPage());
    }

    int pageCount() {
        return pageCount;
    }

    OptionalInt firstRowOnLastPage() {
        return firstRowOnLastPage;
    }

    /** Tracks, for the page currently being read, the lowest position id found on it. */
    private static final class PageHandler extends DefaultHandler {

        private int pageCount;
        private int firstRowOnCurrentPage = Integer.MAX_VALUE;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (IntermediateFormat.isElement(uri, localName, IntermediateFormat.PAGE)) {
                pageCount++;
                firstRowOnCurrentPage = Integer.MAX_VALUE;
            } else if (IntermediateFormat.isElement(uri, localName, IntermediateFormat.ID)) {
                String id = attributes.getValue(IntermediateFormat.ID_NAME_ATTRIBUTE);
                if (id != null && id.startsWith(ChunkTemplateContract.ROW_ID_PREFIX)) {
                    int row = Integer.parseInt(id.substring(ChunkTemplateContract.ROW_ID_PREFIX.length()));
                    firstRowOnCurrentPage = Math.min(firstRowOnCurrentPage, row);
                }
            }
        }

        OptionalInt firstRowOnLastPage() {
            return firstRowOnCurrentPage == Integer.MAX_VALUE ? OptionalInt.empty() : OptionalInt.of(firstRowOnCurrentPage);
        }
    }
}
