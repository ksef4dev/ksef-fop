package io.alapierre.ksef.fop.internal.chunk;

import java.util.Arrays;

/**
 * One page of a laid-out chunk: its place in the chunk, its number and master, and the first candidate row that
 * starts on it together with a digest of everything drawn on the page before that row.
 */
final class LaidOutPage {

    final int sequenceIndex;
    final int indexInSequence;
    final String name;
    final String masterName;
    int firstRow = -1;
    byte[] digestBeforeFirstRow;

    LaidOutPage(int sequenceIndex, int indexInSequence, String name, String masterName) {
        this.sequenceIndex = sequenceIndex;
        this.indexInSequence = indexInSequence;
        this.name = name;
        this.masterName = masterName;
    }

    boolean hasFirstRow() {
        return firstRow >= 0;
    }

    /**
     * Whether this page starts exactly like {@code other}: same first row, and the same content drawn before it
     * (table header, page number, footer). A chunk cut at that row then loses nothing that was on the page.
     */
    boolean startsLike(LaidOutPage other) {
        return firstRow >= 0 && firstRow == other.firstRow
                && Arrays.equals(digestBeforeFirstRow, other.digestBeforeFirstRow);
    }

    /** The page number as an integer, or {@code -1} when it is not a plain decimal number. */
    int pageNumber() {
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
