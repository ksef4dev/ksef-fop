/*
 * SPDX-License-identifier: Apache-2.0
 */
/**
 * Chunked rendering of long invoices: the XSL-FO produced by any template is split at table rows and laid out
 * chunk by chunk, so FOP keeps only one chunk in memory at a time. Entry point: {@link
 * io.alapierre.ksef.fop.internal.chunk.ChunkedInvoiceRenderer}.
 *
 * <p>These classes are intended for internal use and are not part of the public API. They can be changed or
 * removed without notice in future releases.</p>
 */
package io.alapierre.ksef.fop.internal.chunk;
