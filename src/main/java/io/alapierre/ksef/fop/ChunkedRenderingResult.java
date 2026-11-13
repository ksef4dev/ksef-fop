package io.alapierre.ksef.fop;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

/**
 * Outcome of {@link PdfGenerator#generateInvoiceChunked}: either the invoice was rendered in chunks, or it was
 * rendered in a single pass for the given reason.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChunkedRenderingResult {

    /** {@code true} when the invoice was rendered in chunks. */
    boolean chunked;

    /** Number of chunks; {@code 1} for a single-pass render. */
    int chunkCount;

    /** Pages in the generated PDF; {@code 0} when unknown (single-pass render). */
    int pageCount;

    /** Pages laid out by FOP including dropped ones; {@code 0} when unknown (single-pass render). */
    int laidOutPageCount;

    /** Why the invoice was rendered in a single pass; {@code null} when it was rendered in chunks. */
    @Nullable
    String singlePassReason;

    public static ChunkedRenderingResult chunked(int chunkCount, int pageCount, int laidOutPageCount) {
        return new ChunkedRenderingResult(true, chunkCount, pageCount, laidOutPageCount, null);
    }

    public static ChunkedRenderingResult singlePass(String reason) {
        return new ChunkedRenderingResult(false, 1, 0, 0, reason);
    }
}
