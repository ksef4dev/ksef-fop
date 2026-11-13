package io.alapierre.ksef.fop.internal.chunk;

import io.alapierre.ksef.fop.ChunkedRenderingResult;
import io.alapierre.ksef.fop.internal.XmlFactories;
import lombok.extern.slf4j.Slf4j;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFSerializer;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.render.intermediate.util.IFConcatenator;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders an invoice in chunks of table rows, each laid out separately, so FOP keeps only one chunk in memory.
 * Works with any template: the XSL-FO it produces is split, the template itself is not involved.
 *
 * <ol>
 *   <li>The FO is written to a temporary file ({@link FoSpooler}), which also finds the table rows a chunk may
 *       start at and marks them with ids.</li>
 *   <li>Each chunk is laid out into the FOP intermediate format (IF). A chunk that continues a page sequence reopens
 *       it on the master FOP used for that page ({@link ChunkSource}).</li>
 *   <li>A chunk ends at the last page on which a candidate row starts; that page is dropped and the next chunk
 *       starts with that row. The cut is accepted only if the next chunk's first page draws exactly what the dropped
 *       page drew before that row, so a row broken across pages or text between tables is never lost.</li>
 *   <li>The kept pages of all chunks are concatenated into one PDF.</li>
 * </ol>
 *
 * <p>Whenever the document cannot be chunked, it is rendered in a single pass from the spooled FO, which gives the
 * same PDF as rendering the template directly. Nothing is written to the output before that decision.</p>
 */
@Slf4j
public final class ChunkedInvoiceRenderer {

    private static final int MAX_CUTS_TRIED = 3;

    private final FopFactory fopFactory;
    private final int rowsPerChunk;

    public ChunkedInvoiceRenderer(FopFactory fopFactory, int rowsPerChunk) {
        if (rowsPerChunk < 1) {
            throw new IllegalArgumentException("rowsPerChunk must be positive: " + rowsPerChunk);
        }
        this.fopFactory = fopFactory;
        this.rowsPerChunk = rowsPerChunk;
    }

    /**
     * Transforms {@code invoice} with {@code transformer} and writes the PDF to {@code out}, in chunks when possible.
     */
    public ChunkedRenderingResult render(Transformer transformer, Source invoice, OutputStream out)
            throws TransformerException, FOPException {
        try (TemporaryFiles temporaryFiles = new TemporaryFiles()) {
            SpooledFo fo = spool(transformer, invoice, temporaryFiles.create("ksef-invoice-", ".fo"));
            String reason = whyNotChunkable(fo);
            if (reason == null) {
                try {
                    return renderChunked(fo, out, temporaryFiles);
                } catch (NotChunkableException e) {
                    reason = e.getMessage();
                }
            }
            log.debug("Rendering invoice in a single pass: {}", reason);
            renderSinglePass(fo, out);
            return ChunkedRenderingResult.singlePass(reason);
        } catch (IOException | SAXException | IFException e) {
            throw new TransformerException(e);
        }
    }

    private static SpooledFo spool(Transformer transformer, Source invoice, File target)
            throws IOException, TransformerException {
        FoSpooler spooler = new FoSpooler(target);
        transformer.transform(invoice, new SAXResult(spooler));
        return spooler.finish();
    }

    private String whyNotChunkable(SpooledFo fo) {
        if (fo.notChunkableReason != null) {
            return fo.notChunkableReason;
        }
        if (fopFactory.newFOUserAgent().isAccessibilityEnabled()) {
            return "accessibility (tagged PDF) is enabled";
        }
        if (fo.rows.size() <= rowsPerChunk) {
            return "document has only " + fo.rows.size() + " table rows that can start a chunk";
        }
        return null;
    }

    // ---- chunked ----

    private ChunkedRenderingResult renderChunked(SpooledFo fo, OutputStream out, TemporaryFiles temporaryFiles)
            throws IOException, SAXException, IFException, TransformerException, NotChunkableException {
        ChunkSource source = new ChunkSource(fo);
        int totalRows = fo.rows.size();
        List<KeptPages> kept = new ArrayList<>();
        int laidOutPages = 0;
        RenderedChunk current = layOut(source, ChunkRequest.first(rowsPerChunk, totalRows), temporaryFiles);
        laidOutPages += current.layout.pageCount();
        while (!current.request.endsDocument()) {
            Cut cut = nextChunk(source, fo, current, temporaryFiles);
            if (cut == null) {
                log.debug("No usable cut in chunk {}; widening it", current.request);
                current = layOut(source, current.request.widened(rowsPerChunk, totalRows), temporaryFiles);
            } else {
                kept.add(new KeptPages(current, cut.page));
                current = cut.next;
            }
            laidOutPages += current.layout.pageCount();
        }
        kept.add(new KeptPages(current, current.layout.pageCount()));
        concatenate(kept, out);
        int pages = 0;
        for (KeptPages chunk : kept) {
            pages += chunk.count;
        }
        return ChunkedRenderingResult.chunked(kept.size(), pages, laidOutPages);
    }

    /** Tries the last pages of {@code chunk} as cut points; returns the first cut the next chunk confirms. */
    private Cut nextChunk(ChunkSource source, SpooledFo fo, RenderedChunk chunk, TemporaryFiles temporaryFiles)
            throws IOException, SAXException, TransformerException, NotChunkableException {
        int tried = 0;
        List<LaidOutPage> pages = chunk.layout.pages();
        for (int page = pages.size() - 1; page >= 1 && tried < MAX_CUTS_TRIED; page--) {
            LaidOutPage cutPage = pages.get(page);
            if (!canCutAt(fo, chunk.request, cutPage)) {
                continue;
            }
            tried++;
            ChunkRequest request = ChunkRequest.continuingAt(cutPage.firstRow,
                    continuationAt(chunk, cutPage), rowsPerChunk, fo.rows.size());
            RenderedChunk next = layOut(source, request, temporaryFiles);
            if (next.layout.pageCount() > 0 && next.layout.page(0).startsLike(cutPage)) {
                log.debug("Chunk {}: {} pages kept, next chunk {}", chunk.request, page, request);
                return new Cut(page, next);
            }
            log.debug("Cut at row {} rejected: the next chunk does not start like page {}", cutPage.firstRow, page);
        }
        return null;
    }

    private static boolean canCutAt(SpooledFo fo, ChunkRequest request, LaidOutPage page) {
        return page.hasFirstRow()
                && page.firstRow > request.firstRow
                && fo.rows.get(page.firstRow).followsCandidateRow
                && page.pageNumber() > 0;
    }

    /** How the page sequence of {@code page} continues in the next chunk. */
    private static Continuation continuationAt(RenderedChunk chunk, LaidOutPage page) {
        int pagesBefore = page.indexInSequence;
        if (page.sequenceIndex == 0 && chunk.request.continuation != null) {
            pagesBefore += chunk.request.continuation.pagesBefore;
        }
        return new Continuation(page.pageNumber(), page.masterName, pagesBefore);
    }

    private RenderedChunk layOut(ChunkSource source, ChunkRequest request, TemporaryFiles temporaryFiles)
            throws IOException, SAXException, NotChunkableException {
        File intermediateFormat = temporaryFiles.create("ksef-invoice-chunk-", ".if.xml");
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        try (InputStream fo = source.open(request);
             OutputStream out = new BufferedOutputStream(new FileOutputStream(intermediateFormat))) {
            userAgent.setDocumentHandlerOverride(intermediateFormatSerializer(userAgent));
            Fop fop = fopFactory.newFop(MimeConstants.MIME_FOP_IF, userAgent, out);
            parse(new InputSource(fo), fop.getDefaultHandler());
        } catch (SAXException e) {
            throw new NotChunkableException("laying out chunk " + request + " failed: " + e.getMessage());
        }
        return new RenderedChunk(request, intermediateFormat, ChunkLayout.read(intermediateFormat));
    }

    /** IF serializer laying out with the PDF renderer's fonts and metrics, so the final PDF matches. */
    private IFSerializer intermediateFormatSerializer(FOUserAgent userAgent) throws FOPException {
        IFDocumentHandler pdfHandler = fopFactory.getRendererFactory()
                .createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
        IFSerializer serializer = new IFSerializer(new IFContext(userAgent));
        serializer.mimicDocumentHandler(pdfHandler);
        return serializer;
    }

    private void concatenate(List<KeptPages> chunks, OutputStream out)
            throws IFException, FOPException, TransformerException, SAXException {
        IFDocumentHandler pdfHandler = fopFactory.getRendererFactory()
                .createDocumentHandler(fopFactory.newFOUserAgent(), MimeConstants.MIME_PDF);
        pdfHandler.setResult(new StreamResult(out));
        IFUtil.setupFonts(pdfHandler);
        IFConcatenator concatenator = new IFConcatenator(pdfHandler, null);
        for (KeptPages chunk : chunks) {
            concatenator.appendDocument(keptPages(chunk));
        }
        concatenator.finish();
    }

    private static Source keptPages(KeptPages chunk) throws SAXException {
        PageDroppingFilter filter = new PageDroppingFilter(newXmlReader(), chunk.count);
        return new SAXSource(filter, new InputSource(chunk.chunk.intermediateFormat.toURI().toString()));
    }

    // ---- single pass ----

    private void renderSinglePass(SpooledFo fo, OutputStream out) throws IOException, SAXException, FOPException {
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, fopFactory.newFOUserAgent(), out);
        RowIdStrippingFilter filter = new RowIdStrippingFilter(newXmlReader());
        filter.setContentHandler(fop.getDefaultHandler());
        filter.parse(new InputSource(fo.file.toURI().toString()));
    }

    // ---- helpers ----

    private static void parse(InputSource input, ContentHandler handler) throws IOException, SAXException {
        XMLReader reader = newXmlReader();
        reader.setContentHandler(handler);
        reader.parse(input);
    }

    private static XMLReader newXmlReader() throws SAXException {
        try {
            return XmlFactories.SAX_PARSER_FACTORY.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }

    /** An accepted cut: pages of the current chunk kept, and the already laid-out next chunk. */
    private static final class Cut {
        final int page;
        final RenderedChunk next;

        Cut(int page, RenderedChunk next) {
            this.page = page;
            this.next = next;
        }
    }
}
