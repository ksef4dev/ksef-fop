package io.alapierre.ksef.fop.internal.chunk;

/**
 * Element names of the FOP intermediate format (IF) used by chunked rendering.
 */
final class IntermediateFormat {

    static final String NAMESPACE = "http://xmlgraphics.apache.org/fop/intermediate";
    static final String PAGE = "page";
    static final String ID = "id";
    static final String ID_NAME_ATTRIBUTE = "name";

    private IntermediateFormat() {
    }

    static boolean isElement(String uri, String localName, String expectedLocalName) {
        return NAMESPACE.equals(uri) && expectedLocalName.equals(localName);
    }
}
