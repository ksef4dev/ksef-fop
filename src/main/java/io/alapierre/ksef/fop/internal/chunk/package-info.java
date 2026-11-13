/*
 * SPDX-License-identifier: Apache-2.0
 */
/**
 * Chunked rendering of long invoices: the position table is laid out in chunks, each as its own page
 * sequence, cut at real page breaks, so FOP keeps only one chunk in memory at a time.
 *
 * <p>These classes are intended for internal use and are not part of the public API. They can be changed or
 * removed without notice in future releases.</p>
 */
package io.alapierre.ksef.fop.internal.chunk;
