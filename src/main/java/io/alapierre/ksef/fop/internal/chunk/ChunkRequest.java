package io.alapierre.ksef.fop.internal.chunk;

/**
 * Which part of the spooled FO a chunk renders: from a row (or the document start) to a row (or the document end).
 */
final class ChunkRequest {

    private static final int DOCUMENT_START = -1;
    private static final int DOCUMENT_END = -1;

    final int firstRow;
    final int lastRow;
    final Continuation continuation;

    private ChunkRequest(int firstRow, int lastRow, Continuation continuation) {
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.continuation = continuation;
    }

    /** The first chunk: the document start up to {@code rowsPerChunk} candidate rows. */
    static ChunkRequest first(int rowsPerChunk, int totalRows) {
        return new ChunkRequest(DOCUMENT_START, lastRow(rowsPerChunk - 1, totalRows), null);
    }

    /** A chunk resuming at {@code firstRow} with {@code rowsPerChunk} candidate rows. */
    static ChunkRequest continuingAt(int firstRow, Continuation continuation, int rowsPerChunk, int totalRows) {
        return new ChunkRequest(firstRow, lastRow(firstRow + rowsPerChunk - 1, totalRows), continuation);
    }

    /** The same chunk with {@code extraRows} more candidate rows. */
    ChunkRequest widened(int extraRows, int totalRows) {
        return new ChunkRequest(firstRow, lastRow(lastRow + extraRows, totalRows), continuation);
    }

    boolean startsDocument() {
        return firstRow == DOCUMENT_START;
    }

    boolean endsDocument() {
        return lastRow == DOCUMENT_END;
    }

    private static int lastRow(int row, int totalRows) {
        return row >= totalRows - 1 ? DOCUMENT_END : row;
    }

    @Override
    public String toString() {
        String from = startsDocument() ? "start" : "row " + firstRow;
        String to = endsDocument() ? "end" : "row " + lastRow;
        return from + ".." + to;
    }
}
