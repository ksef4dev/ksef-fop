package io.alapierre.ksef.fop.internal.chunk;

import io.alapierre.ksef.fop.ChunkedRenderingResult;
import io.alapierre.ksef.fop.internal.XmlFactories;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.om.NodeInfo;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders a long invoice as several page sequences without partially filled pages at chunk boundaries.
 *
 * <ol>
 *   <li>Each chunk of positions is laid out by FOP into the intermediate format (IF) in a temporary file.</li>
 *   <li>Position ids ({@value ChunkTemplateContract#ROW_ID_PREFIX}N) in the IF tell on which page every
 *       position starts.</li>
 *   <li>The last, partially filled page of a chunk is dropped; the next chunk starts with its first position,
 *       so every cut falls on a page break FOP would have made anyway.</li>
 *   <li>The final chunk carries the rest of the invoice. All chunks are concatenated into one PDF.</li>
 * </ol>
 *
 * <p>FOP keeps only one chunk in memory at a time. The template must follow {@link ChunkTemplateContract}.
 * Nothing is written to the output until every chunk has been laid out, so a {@link NotChunkableException}
 * leaves the output untouched.</p>
 */
@Slf4j
public final class ChunkedInvoiceRenderer {

    private static final String TEMP_FILE_PREFIX = "ksef-invoice-chunk-";
    private static final String TEMP_FILE_SUFFIX = ".if.xml";

    /** Supplies a transformer with all invoice parameters applied; called once per chunk. */
    @FunctionalInterface
    public interface InvoiceTransformerSupplier {
        Transformer newTransformer() throws TransformerException;
    }

    private final FopFactory fopFactory;
    private final int rowsPerChunk;

    public ChunkedInvoiceRenderer(FopFactory fopFactory, int rowsPerChunk) {
        if (rowsPerChunk < 1) {
            throw new IllegalArgumentException("rowsPerChunk must be positive: " + rowsPerChunk);
        }
        this.fopFactory = fopFactory;
        this.rowsPerChunk = rowsPerChunk;
    }

    public ChunkedRenderingResult render(byte[] invoiceXml, InvoiceTransformerSupplier transformers, OutputStream out)
            throws TransformerException, FOPException, NotChunkableException {
        int totalRows = countRows(invoiceXml);
        if (totalRows <= rowsPerChunk) {
            throw new NotChunkableException("invoice has only " + totalRows + " positions");
        }
        try (TemporaryFiles temporaryFiles = new TemporaryFiles()) {
            List<RenderedChunk> chunks = layOutChunks(invoiceXml, totalRows, transformers, temporaryFiles);
            concatenate(chunks, out);
            return ChunkedRenderingResult.chunked(chunks.size(), pagesToKeep(chunks), laidOutPages(chunks));
        } catch (IOException | SAXException | IFException e) {
            throw new TransformerException(e);
        }
    }

    private List<RenderedChunk> layOutChunks(byte[] invoiceXml, int totalRows, InvoiceTransformerSupplier transformers,
                                             TemporaryFiles temporaryFiles)
            throws TransformerException, FOPException, IOException, SAXException, NotChunkableException {
        List<RenderedChunk> chunks = new ArrayList<>();
        Transformer transformer = transformers.newTransformer();
        NodeInfo invoice = parseOnce(transformer, invoiceXml);
        ChunkWindow window = ChunkWindow.startingAt(1, rowsPerChunk, totalRows);
        int pagesSoFar = 0;
        while (true) {
            window.applyTo(transformer, pagesSoFar + 1);
            File intermediateFormat = temporaryFiles.create(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            layOut(transformer, invoice, intermediateFormat);
            LaidOutPages pages = LaidOutPages.read(intermediateFormat);
            if (window.isFinal()) {
                chunks.add(new RenderedChunk(intermediateFormat, pages.pageCount(), pages.pageCount()));
                return chunks;
            }
            if (!spansPageBreak(window, pages)) {
                log.debug("Chunk {} fits on one page; widening it", window);
                window = window.widened();
                transformer = transformers.newTransformer();
                continue;
            }
            int nextRow = firstRowOfNextChunk(pages);
            chunks.add(new RenderedChunk(intermediateFormat, pages.pageCount(), pages.pageCount() - 1));
            log.debug("Chunk {}: {} pages laid out, next chunk starts at position {}", window, pages.pageCount(), nextRow);
            pagesSoFar += pages.pageCount() - 1;
            window = ChunkWindow.startingAt(nextRow, rowsPerChunk, totalRows);
            transformer = transformers.newTransformer();
        }
    }

    /** A chunk can be cut only when a later position starts on a page other than the first one. */
    private static boolean spansPageBreak(ChunkWindow window, LaidOutPages pages) throws NotChunkableException {
        return pages.pageCount() > 1 && firstRowOfNextChunk(pages) > window.firstRow();
    }

    /** The first position on the last (dropped) page starts the next chunk. */
    private static int firstRowOfNextChunk(LaidOutPages pages) throws NotChunkableException {
        return pages.firstRowOnLastPage().orElseThrow(() -> new NotChunkableException(
                "template did not mark positions with " + ChunkTemplateContract.ROW_ID_PREFIX + "N ids"));
    }

    /** Parses the invoice once into the transformer's Saxon configuration so every chunk reuses the tree. */
    private static NodeInfo parseOnce(Transformer transformer, byte[] invoiceXml)
            throws TransformerException, NotChunkableException {
        if (!(transformer instanceof TransformerImpl)) {
            throw new NotChunkableException("transformer is not a Saxon transformer");
        }
        StreamSource source = new StreamSource(new ByteArrayInputStream(invoiceXml));
        return ((TransformerImpl) transformer).getConfiguration().buildDocumentTree(source).getRootNode();
    }

    private void layOut(Transformer transformer, NodeInfo invoice, File target)
            throws IOException, FOPException, TransformerException {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.setDocumentHandlerOverride(intermediateFormatSerializer(userAgent));
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(target))) {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_FOP_IF, userAgent, out);
            transformer.transform(invoice, new SAXResult(fop.getDefaultHandler()));
        }
    }

    /** IF serializer laying out with the PDF renderer's fonts and metrics, so the final PDF matches. */
    private IFSerializer intermediateFormatSerializer(FOUserAgent userAgent) throws FOPException {
        IFDocumentHandler pdfHandler = fopFactory.getRendererFactory()
                .createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
        IFSerializer serializer = new IFSerializer(new IFContext(userAgent));
        serializer.mimicDocumentHandler(pdfHandler);
        return serializer;
    }

    private void concatenate(List<RenderedChunk> chunks, OutputStream out)
            throws IFException, FOPException, TransformerException, SAXException {
        IFDocumentHandler pdfHandler = fopFactory.getRendererFactory()
                .createDocumentHandler(fopFactory.newFOUserAgent(), MimeConstants.MIME_PDF);
        pdfHandler.setResult(new StreamResult(out));
        IFUtil.setupFonts(pdfHandler);
        IFConcatenator concatenator = new IFConcatenator(pdfHandler, null);
        for (RenderedChunk chunk : chunks) {
            concatenator.appendDocument(keptPages(chunk));
        }
        concatenator.finish();
    }

    private static Source keptPages(RenderedChunk chunk) throws SAXException {
        PageDroppingFilter filter = new PageDroppingFilter(newXmlReader(), chunk.pagesToKeep());
        return new SAXSource(filter, new InputSource(chunk.intermediateFormat().toURI().toString()));
    }

    private static XMLReader newXmlReader() throws SAXException {
        try {
            return XmlFactories.SAX_PARSER_FACTORY.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }

    private static int countRows(byte[] invoiceXml) throws TransformerException {
        try {
            return InvoiceRowCounter.countRows(invoiceXml);
        } catch (XMLStreamException e) {
            throw new TransformerException(e);
        }
    }

    private static int pagesToKeep(List<RenderedChunk> chunks) {
        int pages = 0;
        for (RenderedChunk chunk : chunks) {
            pages += chunk.pagesToKeep();
        }
        return pages;
    }

    private static int laidOutPages(List<RenderedChunk> chunks) {
        int pages = 0;
        for (RenderedChunk chunk : chunks) {
            pages += chunk.laidOutPages();
        }
        return pages;
    }
}
