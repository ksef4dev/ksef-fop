package io.alapierre.ksef.fop.internal.chunk;

import java.util.Collections;
import java.util.List;

/**
 * An element of the spooled FO document, with what chunking needs to know about it.
 *
 * <p>Attributes are kept only for elements a continuation chunk reopens (page sequence, flow, block, table, table
 * body); other elements keep just their name and parent.</p>
 */
final class FoElement {

    final String uri;
    final String localName;
    final FoElement parent;
    final List<FoAttribute> attributes;

    long startTagStart;
    long startTagEnd;
    int childCount;

    // fo:table
    int columnCount;
    long headerStart = -1;
    long headerEnd = -1;
    long firstBodyStart = -1;

    // fo:table-body
    Boolean chunkable;
    int rowsCoveredBySpans;
    boolean previousRowWasCandidate;
    FoRow lastCandidateRow;

    // fo:table-row
    int rowIndex = -1;
    int cellCount;
    int maxRowSpan = 1;

    // fo:table-cell
    boolean firstCellOfCandidateRow;

    FoElement(String uri, String localName, FoElement parent, List<FoAttribute> attributes) {
        this.uri = uri;
        this.localName = localName;
        this.parent = parent;
        this.attributes = attributes == null ? Collections.<FoAttribute>emptyList() : attributes;
    }

    boolean is(String foLocalName) {
        return FoNames.isFo(uri, localName, foLocalName);
    }

    String attribute(String name) {
        for (FoAttribute attribute : attributes) {
            if (attribute.isUnqualified(name)) {
                return attribute.value;
            }
        }
        return null;
    }
}
