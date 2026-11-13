package io.alapierre.ksef.fop.internal.chunk;

/**
 * One attribute of a spooled FO element, kept for elements that may have to be reopened in a continuation chunk.
 */
final class FoAttribute {

    final String uri;
    final String localName;
    final String value;

    FoAttribute(String uri, String localName, String value) {
        this.uri = uri;
        this.localName = localName;
        this.value = value;
    }

    boolean isUnqualified(String name) {
        return uri.isEmpty() && localName.equals(name);
    }
}
