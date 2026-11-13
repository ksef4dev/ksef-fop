package io.alapierre.ksef.fop.internal.chunk;

/**
 * Namespaces and names shared by the chunked-rendering classes.
 */
final class FoNames {

    static final String FO_NAMESPACE = "http://www.w3.org/1999/XSL/Format";
    static final String FOX_NAMESPACE = "http://xmlgraphics.apache.org/fop/extensions";
    static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    static final String IF_NAMESPACE = "http://xmlgraphics.apache.org/fop/intermediate";

    /** Id put on the first block of every row that may start a chunk; followed by the row index. */
    static final String ROW_ID_PREFIX = "ksef-chunk-row-";

    /** Name of the page-sequence-master added to continuation chunks. */
    static final String CONTINUATION_MASTER = "ksef-chunk-continuation";

    private FoNames() {
    }

    static boolean isFo(String uri, String localName, String expected) {
        return FO_NAMESPACE.equals(uri) && expected.equals(localName);
    }

    static boolean isIf(String uri, String localName, String expected) {
        return IF_NAMESPACE.equals(uri) && expected.equals(localName);
    }
}
