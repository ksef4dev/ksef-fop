package io.alapierre.ksef.fop.internal.chunk;

import java.io.File;

/**
 * A chunk laid out into an intermediate-format file.
 */
final class RenderedChunk {

    final ChunkRequest request;
    final File intermediateFormat;
    final ChunkLayout layout;

    RenderedChunk(ChunkRequest request, File intermediateFormat, ChunkLayout layout) {
        this.request = request;
        this.intermediateFormat = intermediateFormat;
        this.layout = layout;
    }
}
