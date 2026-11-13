package io.alapierre.ksef.fop.internal.chunk;

/**
 * The invoice cannot be rendered in chunks (template without chunk support, too few positions, no page break
 * that could be cut at). Nothing has been written to the output yet, so the caller can render in a single pass.
 */
public class NotChunkableException extends Exception {

    private static final long serialVersionUID = 1L;

    NotChunkableException(String message) {
        super(message);
    }
}
