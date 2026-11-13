package io.alapierre.ksef.fop.internal.chunk;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Receives the XSL-FO produced by the invoice template, writes it to a file and finds the table rows a chunk may
 * start at.
 *
 * <p>A row is a candidate when its table can be reopened in a new page sequence without changing the layout: the
 * table sits directly in the flow (only plain blocks around it), has explicit columns, neither the table nor the
 * blocks around it draw borders or padding, the page sequence uses a supported master, and no row span from an
 * earlier row covers it. The first block of each candidate row gets the id {@link FoNames#ROW_ID_PREFIX}N, so the
 * laid-out pages tell where each row starts.</p>
 *
 * <p>The document as a whole cannot be chunked when it refers across pages (page-number citations, markers,
 * internal links, bookmarks) or uses multi-column regions.</p>
 */
final class FoSpooler extends DefaultHandler {

    private static final Set<String> REOPENED_ELEMENTS = new HashSet<>(Arrays.asList(
            "page-sequence", "flow", "block", "table", "table-body"));
    private static final Set<String> CROSS_PAGE_ELEMENTS = new HashSet<>(Arrays.asList(
            "page-number-citation", "page-number-citation-last", "retrieve-marker", "retrieve-table-marker",
            "bookmark-tree", "index-page-citation-list", "index-key-reference", "change-bar-begin"));
    private static final Set<String> ALLOWED_EDGE_ATTRIBUTES = new HashSet<>(Arrays.asList(
            "border-collapse", "border-separation", "border-spacing"));

    private final CountingXmlWriter out;
    private final File file;
    private final Deque<FoElement> open = new ArrayDeque<>();
    private final Map<String, String> prefixes = new HashMap<>();
    private final PageMasters pageMasters = new PageMasters();
    private final List<FoRow> rows = new ArrayList<>();
    private FoRow currentRow;
    private long layoutMasterSetEnd = -1;
    private long firstPageSequenceStart = -1;
    private String notChunkableReason;

    FoSpooler(File file) throws IOException {
        this.file = file;
        this.out = new CountingXmlWriter(file);
    }

    SpooledFo finish() throws IOException {
        out.close();
        String reason = notChunkableReason;
        if (reason == null && (layoutMasterSetEnd < 0 || firstPageSequenceStart < 0)) {
            reason = "no layout-master-set or page sequence";
        }
        return new SpooledFo(file, out.position(), layoutMasterSetEnd, firstPageSequenceStart, rows, pageMasters, reason);
    }

    // ---- ContentHandler ----

    @Override
    public void startDocument() throws SAXException {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        FoElement parent = open.peek();
        boolean fo = FoNames.FO_NAMESPACE.equals(uri);
        FoElement element = new FoElement(uri, localName, parent,
                fo && REOPENED_ELEMENTS.contains(localName) ? copy(attributes) : null);
        if (parent != null) {
            parent.childCount++;
        }
        if (fo) {
            inspect(element, attributes);
        } else if (FoNames.FOX_NAMESPACE.equals(uri) && "destination".equals(localName)) {
            notChunkable("fox:destination");
        }
        element.startTagStart = out.position();
        write(startTag(element, attributes, rowIdFor(element, attributes)));
        element.startTagEnd = out.position();
        open.push(element);
        if (fo) {
            trackTableStructure(element);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        FoElement element = open.pop();
        if (element.is("layout-master-set")) {
            layoutMasterSetEnd = out.position();
        }
        write("</" + qualifiedName(element.uri, element.localName) + ">");
        if (FoNames.FO_NAMESPACE.equals(uri)) {
            afterEndTag(element);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            out.writeText(ch, start, length);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        characters(ch, start, length);
    }

    // ---- analysis ----

    /** Looks at an FO element before its start tag is written. */
    private void inspect(FoElement element, Attributes attributes) {
        String name = element.localName;
        if (CROSS_PAGE_ELEMENTS.contains(name)) {
            notChunkable("fo:" + name);
            return;
        }
        switch (name) {
            case "basic-link":
                if (attributes.getValue("internal-destination") != null) {
                    notChunkable("internal link");
                }
                break;
            case "region-body":
                if (isMultiColumn(attributes.getValue("column-count"))) {
                    notChunkable("multi-column region");
                }
                break;
            case "page-sequence":
                if (firstPageSequenceStart < 0) {
                    firstPageSequenceStart = out.position();
                }
                break;
            case "simple-page-master":
                pageMasters.addSimpleMaster(attributes.getValue("master-name"));
                break;
            case "page-sequence-master":
                pageMasters.startSequenceMaster(attributes.getValue("master-name"));
                break;
            case "single-page-master-reference":
                pageMasters.addSingleReference(attributes.getValue("master-reference"));
                break;
            case "repeatable-page-master-reference":
                pageMasters.addRepeatableReference(attributes.getValue("master-reference"),
                        attributes.getValue("maximum-repeats"));
                break;
            case "repeatable-page-master-alternatives":
                pageMasters.startAlternatives(attributes.getValue("maximum-repeats"));
                break;
            case "conditional-page-master-reference":
                pageMasters.addConditional(unqualified(attributes));
                break;
            case "table-cell":
                if (element.parent != null && element.parent.is("table-row")) {
                    trackRowSpan(element.parent, attributes.getValue("number-rows-spanned"));
                }
                break;
            default:
                break;
        }
    }

    /** Table bookkeeping that needs the offsets of the start tag just written. */
    private void trackTableStructure(FoElement element) {
        FoElement parent = element.parent;
        switch (element.localName) {
            case "table-column":
                if (parent != null && parent.is("table")) {
                    parent.columnCount++;
                }
                break;
            case "table-header":
                if (parent != null && parent.is("table")) {
                    parent.headerStart = element.startTagStart;
                }
                break;
            case "table-body":
                if (parent != null && parent.is("table") && parent.firstBodyStart < 0) {
                    parent.firstBodyStart = element.startTagStart;
                }
                break;
            case "table-row":
                startRow(element);
                break;
            case "table-cell":
                startCell(element);
                break;
            default:
                break;
        }
    }

    private void afterEndTag(FoElement element) {
        switch (element.localName) {
            case "page-sequence-master":
                pageMasters.endSequenceMaster();
                break;
            case "repeatable-page-master-alternatives":
                pageMasters.endAlternatives();
                break;
            case "table-header":
                if (element.parent != null && element.parent.is("table")) {
                    element.parent.headerEnd = out.position();
                }
                break;
            case "table-row":
                endRow(element);
                break;
            default:
                break;
        }
    }

    private void startRow(FoElement row) {
        FoElement body = row.parent;
        if (body == null || !body.is("table-body")) {
            return;
        }
        if (body.chunkable == null) {
            body.chunkable = isChunkableBody(body);
        }
        if (body.chunkable && body.rowsCoveredBySpans == 0) {
            row.rowIndex = rows.size();
            currentRow = new FoRow(row.rowIndex, row.startTagStart, body, body.previousRowWasCandidate);
            rows.add(currentRow);
        }
    }

    private void endRow(FoElement row) {
        FoElement body = row.parent;
        if (body == null || !body.is("table-body")) {
            return;
        }
        if (row.rowIndex >= 0) {
            body.lastCandidateRow = currentRow;
        }
        if (body.lastCandidateRow != null) {
            body.lastCandidateRow.end = out.position();
        }
        body.previousRowWasCandidate = row.rowIndex >= 0;
        body.rowsCoveredBySpans = Math.max(0, Math.max(body.rowsCoveredBySpans - 1, row.maxRowSpan - 1));
    }

    private void startCell(FoElement cell) {
        FoElement row = cell.parent;
        if (row == null || !row.is("table-row")) {
            return;
        }
        row.cellCount++;
        cell.firstCellOfCandidateRow = row.rowIndex >= 0 && row.cellCount == 1;
    }

    private static void trackRowSpan(FoElement row, String spanned) {
        if (spanned != null) {
            try {
                row.maxRowSpan = Math.max(row.maxRowSpan, Integer.parseInt(spanned.trim()));
            } catch (NumberFormatException ignored) {
                row.maxRowSpan = Integer.MAX_VALUE / 2;
            }
        }
    }

    /** Id for the first block of the first cell of a candidate row, or {@code null}. */
    private String rowIdFor(FoElement element, Attributes attributes) {
        FoElement parent = element.parent;
        if (parent != null && parent.is("table-cell") && parent.firstCellOfCandidateRow && parent.childCount == 1
                && (element.is("block") || element.is("block-container")) && attributes.getValue("id") == null) {
            return FoNames.ROW_ID_PREFIX + parent.parent.rowIndex;
        }
        return null;
    }

    private boolean isChunkableBody(FoElement body) {
        FoElement table = body.parent;
        if (table == null || !table.is("table") || table.columnCount == 0 || hasEdgeAttributes(table)) {
            return false;
        }
        FoElement ancestor = table.parent;
        while (ancestor != null && ancestor.is("block")) {
            if (hasEdgeAttributes(ancestor) || keepsTogether(ancestor) || ancestor.attribute("span") != null) {
                return false;
            }
            ancestor = ancestor.parent;
        }
        if (ancestor == null || !ancestor.is("flow")) {
            return false;
        }
        FoElement pageSequence = ancestor.parent;
        return pageSequence != null && pageSequence.is("page-sequence")
                && pageMasters.supports(pageSequence.attribute("master-reference"));
    }

    private static boolean hasEdgeAttributes(FoElement element) {
        for (FoAttribute attribute : element.attributes) {
            String name = attribute.localName;
            if ((name.startsWith("border") || name.startsWith("padding"))
                    && !ALLOWED_EDGE_ATTRIBUTES.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean keepsTogether(FoElement element) {
        for (FoAttribute attribute : element.attributes) {
            if (attribute.localName.startsWith("keep-together") && !"auto".equals(attribute.value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMultiColumn(String columnCount) {
        return columnCount != null && !columnCount.trim().isEmpty() && !"1".equals(columnCount.trim());
    }

    private void notChunkable(String reason) {
        if (notChunkableReason == null) {
            notChunkableReason = "document uses " + reason;
        }
    }

    // ---- serialization ----

    private String startTag(FoElement element, Attributes attributes, String extraId) {
        StringBuilder tag = new StringBuilder("<").append(qualifiedName(element.uri, element.localName));
        Map<String, String> declarations = new LinkedHashMap<>();
        if (element.parent == null) {
            declarations.put("fo", FoNames.FO_NAMESPACE);
        }
        declarePrefix(element.uri, declarations);
        for (int i = 0; i < attributes.getLength(); i++) {
            String qName = attributes.getQName(i);
            if (qName.startsWith("xmlns")) {
                continue;
            }
            String uri = attributes.getURI(i);
            String local = attributes.getLocalName(i);
            declarePrefix(uri, declarations);
            tag.append(' ').append(qualifiedAttributeName(uri, local)).append("=\"")
                    .append(CountingXmlWriter.escapeAttribute(attributes.getValue(i))).append('"');
        }
        if (extraId != null) {
            tag.append(" id=\"").append(extraId).append('"');
        }
        for (Map.Entry<String, String> declaration : declarations.entrySet()) {
            tag.append(" xmlns:").append(declaration.getKey()).append("=\"")
                    .append(CountingXmlWriter.escapeAttribute(declaration.getValue())).append('"');
        }
        return tag.append('>').toString();
    }

    private void declarePrefix(String uri, Map<String, String> declarations) {
        if (uri.isEmpty() || FoNames.FO_NAMESPACE.equals(uri) || FoNames.XML_NAMESPACE.equals(uri)) {
            return;
        }
        declarations.put(prefixFor(uri), uri);
    }

    private String qualifiedName(String uri, String localName) {
        if (FoNames.FO_NAMESPACE.equals(uri)) {
            return "fo:" + localName;
        }
        return uri.isEmpty() ? localName : prefixFor(uri) + ":" + localName;
    }

    private String qualifiedAttributeName(String uri, String localName) {
        if (uri.isEmpty()) {
            return localName;
        }
        if (FoNames.XML_NAMESPACE.equals(uri)) {
            return "xml:" + localName;
        }
        return qualifiedName(uri, localName);
    }

    private String prefixFor(String uri) {
        String prefix = prefixes.get(uri);
        if (prefix == null) {
            prefix = "ns" + (prefixes.size() + 1);
            prefixes.put(uri, prefix);
        }
        return prefix;
    }

    private static Map<String, String> unqualified(Attributes attributes) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.getURI(i).isEmpty() && !attributes.getQName(i).startsWith("xmlns")) {
                map.put(attributes.getLocalName(i), attributes.getValue(i));
            }
        }
        return map;
    }

    private static List<FoAttribute> copy(Attributes attributes) {
        List<FoAttribute> copy = new ArrayList<>(attributes.getLength());
        for (int i = 0; i < attributes.getLength(); i++) {
            if (!attributes.getQName(i).startsWith("xmlns")) {
                copy.add(new FoAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getValue(i)));
            }
        }
        return copy;
    }

    private void write(String markup) throws SAXException {
        try {
            out.write(markup);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
}
