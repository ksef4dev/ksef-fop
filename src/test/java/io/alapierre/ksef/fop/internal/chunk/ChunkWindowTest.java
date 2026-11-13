package io.alapierre.ksef.fop.internal.chunk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkWindowTest {

    @Test
    void firstChunkOfManyIsHead() {
        ChunkWindow window = ChunkWindow.startingAt(1, 100, 250);

        assertFalse(window.isFinal());
        assertEquals("rows 1..100", window.toString());
    }

    @Test
    void chunkReachingTheLastRowIsFinal() {
        assertTrue(ChunkWindow.startingAt(151, 100, 250).isFinal());
        assertTrue(ChunkWindow.startingAt(151, 100, 250).toString().endsWith("..end"));
        assertFalse(ChunkWindow.startingAt(101, 100, 250).isFinal());
    }

    @Test
    void invoiceFittingInOneChunkIsFinalFromTheStart() {
        assertTrue(ChunkWindow.startingAt(1, 100, 100).isFinal());
    }
}
