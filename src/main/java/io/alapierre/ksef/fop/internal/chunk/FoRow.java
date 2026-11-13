package io.alapierre.ksef.fop.internal.chunk;

/**
 * A table row where a chunk may start: its byte range in the spooled FO and the table body it belongs to. The
 * range also covers the following rows that cannot start a chunk because a row span reaches into them.
 */
final class FoRow {

    final int index;
    final long start;
    final FoElement body;
    final boolean followsCandidateRow;
    long end;

    FoRow(int index, long start, FoElement body, boolean followsCandidateRow) {
        this.index = index;
        this.start = start;
        this.body = body;
        this.followsCandidateRow = followsCandidateRow;
    }
}
