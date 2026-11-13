package io.alapierre.ksef.fop.internal.chunk;

import java.io.File;

/**
 * A chunk laid out into an intermediate-format file, with the number of its pages that go into the final PDF.
 */
final class RenderedChunk {

    private final File intermediateFormat;
    private final int laidOutPages;
    private final int pagesToKeep;

    RenderedChunk(File intermediateFormat, int laidOutPages, int pagesToKeep) {
        this.intermediateFormat = intermediateFormat;
        this.laidOutPages = laidOutPages;
        this.pagesToKeep = pagesToKeep;
    }

    File intermediateFormat() {
        return intermediateFormat;
    }

    int laidOutPages() {
        return laidOutPages;
    }

    int pagesToKeep() {
        return pagesToKeep;
    }
}
