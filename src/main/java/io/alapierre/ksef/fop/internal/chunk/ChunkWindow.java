package io.alapierre.ksef.fop.internal.chunk;

import javax.xml.transform.Transformer;

/**
 * Range of invoice positions rendered by one chunk, together with the template mode it implies.
 */
final class ChunkWindow {

    private static final int UNTIL_LAST_ROW = 0;

    private final int firstRow;
    private final int rowCount;
    private final int totalRows;
    private final boolean finalChunk;
    private final String mode;

    private ChunkWindow(int firstRow, int rowCount, int totalRows) {
        this.firstRow = firstRow;
        this.rowCount = rowCount;
        this.totalRows = totalRows;
        this.finalChunk = firstRow + rowCount - 1 >= totalRows;
        this.mode = modeFor(firstRow == 1, finalChunk);
    }

    /** Window of {@code rowsPerChunk} positions starting at {@code firstRow} (1-based). */
    static ChunkWindow startingAt(int firstRow, int rowsPerChunk, int totalRows) {
        return new ChunkWindow(firstRow, rowsPerChunk, totalRows);
    }

    /** The same start with twice as many positions, for chunks too short to span a page break. */
    ChunkWindow widened() {
        return new ChunkWindow(firstRow, rowCount * 2, totalRows);
    }

    private static String modeFor(boolean firstChunk, boolean finalChunk) {
        if (firstChunk) {
            return finalChunk ? "" : "head";
        }
        return finalChunk ? "tail" : "middle";
    }

    void applyTo(Transformer transformer, int initialPageNumber) {
        transformer.setParameter(ChunkTemplateContract.MODE_PARAM, mode);
        transformer.setParameter(ChunkTemplateContract.FROM_PARAM, firstRow);
        transformer.setParameter(ChunkTemplateContract.TO_PARAM, finalChunk ? UNTIL_LAST_ROW : lastRow());
        transformer.setParameter(ChunkTemplateContract.INITIAL_PAGE_PARAM, initialPageNumber);
    }

    int firstRow() {
        return firstRow;
    }

    private int lastRow() {
        return firstRow + rowCount - 1;
    }

    boolean isFinal() {
        return finalChunk;
    }

    @Override
    public String toString() {
        return finalChunk ? "rows " + firstRow + "..end" : "rows " + firstRow + ".." + lastRow();
    }
}
