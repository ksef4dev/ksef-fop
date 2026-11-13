package io.alapierre.ksef.fop.internal.chunk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * An XSL-FO document written to a temporary file, with the rows a chunk may start at.
 */
final class SpooledFo {

    final File file;
    final long length;
    final long layoutMasterSetEnd;
    final long firstPageSequenceStart;
    final List<FoRow> rows;
    final PageMasters pageMasters;
    final String notChunkableReason;

    SpooledFo(File file, long length, long layoutMasterSetEnd, long firstPageSequenceStart, List<FoRow> rows,
              PageMasters pageMasters, String notChunkableReason) {
        this.file = file;
        this.length = length;
        this.layoutMasterSetEnd = layoutMasterSetEnd;
        this.firstPageSequenceStart = firstPageSequenceStart;
        this.rows = rows;
        this.pageMasters = pageMasters;
        this.notChunkableReason = notChunkableReason;
    }

    byte[] read(long from, long to) throws IOException {
        byte[] bytes = new byte[(int) (to - from)];
        try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
            in.seek(from);
            in.readFully(bytes);
        }
        return bytes;
    }
}
