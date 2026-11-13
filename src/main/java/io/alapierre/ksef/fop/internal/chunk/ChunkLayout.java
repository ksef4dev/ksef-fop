package io.alapierre.ksef.fop.internal.chunk;

import io.alapierre.ksef.fop.internal.XmlFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Pages of a chunk read back from its intermediate-format (IF) file.
 */
final class ChunkLayout {

    private final List<LaidOutPage> pages;

    private ChunkLayout(List<LaidOutPage> pages) {
        this.pages = Collections.unmodifiableList(pages);
    }

    static ChunkLayout read(File intermediateFormat) throws IOException, SAXException {
        PageReader reader = new PageReader();
        try {
            XmlFactories.SAX_PARSER_FACTORY.newSAXParser().parse(intermediateFormat, reader);
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
        return new ChunkLayout(reader.pages);
    }

    List<LaidOutPage> pages() {
        return pages;
    }

    int pageCount() {
        return pages.size();
    }

    LaidOutPage page(int index) {
        return pages.get(index);
    }

    /** Collects pages and digests the IF events drawn on each page before its first candidate row. */
    private static final class PageReader extends DefaultHandler {

        private final List<LaidOutPage> pages = new ArrayList<>();
        private int sequenceIndex = -1;
        private int indexInSequence;
        private LaidOutPage page;
        private MessageDigest digest;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (FoNames.isIf(uri, localName, "page-sequence")) {
                sequenceIndex++;
                indexInSequence = 0;
            } else if (FoNames.isIf(uri, localName, "page")) {
                page = new LaidOutPage(sequenceIndex, indexInSequence++, attributes.getValue("name"),
                        attributes.getValue("page-master-name"));
                pages.add(page);
                digest = newDigest();
                update("page", attributes, "index");
            } else if (digest != null && FoNames.isIf(uri, localName, "id") && isRowId(attributes.getValue("name"))) {
                page.firstRow = Integer.parseInt(attributes.getValue("name").substring(FoNames.ROW_ID_PREFIX.length()));
                page.digestBeforeFirstRow = digest.digest();
                digest = null;
            } else if (digest != null) {
                update(uri + ' ' + localName, attributes, null);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (FoNames.isIf(uri, localName, "page")) {
                digest = null;
            } else if (digest != null) {
                update("/" + localName);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (digest != null) {
                update(new String(ch, start, length));
            }
        }

        private void update(String name, Attributes attributes, String ignoredAttribute) {
            TreeMap<String, String> sorted = new TreeMap<>();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (!attributes.getQName(i).equals(ignoredAttribute)) {
                    sorted.put(attributes.getQName(i), attributes.getValue(i));
                }
            }
            update("<" + name + sorted);
        }

        private void update(String text) {
            digest.update(text.getBytes(StandardCharsets.UTF_8));
        }

        private static boolean isRowId(String id) {
            return id != null && id.startsWith(FoNames.ROW_ID_PREFIX);
        }

        private static MessageDigest newDigest() {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
