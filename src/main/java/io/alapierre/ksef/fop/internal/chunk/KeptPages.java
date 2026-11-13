package io.alapierre.ksef.fop.internal.chunk;

/**
 * The pages of a chunk that go into the final PDF.
 */
final class KeptPages {

    final RenderedChunk chunk;
    final int count;

    KeptPages(RenderedChunk chunk, int count) {
        this.chunk = chunk;
        this.count = count;
    }
}
