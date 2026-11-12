package io.alapierre.ksef.fop;

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
import org.apache.fop.render.intermediate.util.IFConcatenator;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * PROTOTYPE. Renders a long invoice as several page sequences without partially empty pages:
 * <ol>
 *   <li>each chunk of rows is laid out by FOP into the intermediate format (IF) in a temp file;</li>
 *   <li>the IF tells on which page every row starts (ids {@code ksefrow-N});</li>
 *   <li>the last, partially filled page is dropped and the next chunk starts with its first row;</li>
 *   <li>the last chunk carries the rest of the invoice; all IF files are concatenated into one PDF.</li>
 * </ol>
 * FOP keeps only one chunk in memory at a time.
 */
public class ChunkedInvoiceRenderer {

    private static final String IF_NS = "http://xmlgraphics.apache.org/fop/intermediate";
    private static final String ROW_ID_PREFIX = "ksefrow-";

    private final FopFactory factory;
    private final int rowsPerChunk;

    ChunkedInvoiceRenderer(FopFactory factory, int rowsPerChunk) {
        this.factory = factory;
        this.rowsPerChunk = rowsPerChunk;
    }

    interface TransformerSupplier {
        Transformer get() throws TransformerException;
    }

    public static class Stats {
        public int chunks;
        public int pages;
        public int pagesLaidOut;
        public final List<String> log = new ArrayList<>();
    }

    private static class Part {
        final File file;
        final int keepPages;

        Part(File file, int keepPages) {
            this.file = file;
            this.keepPages = keepPages;
        }
    }

    Stats render(byte[] invoiceXml, TransformerSupplier transformers, OutputStream out)
            throws TransformerException, FOPException {
        Stats stats = new Stats();
        List<Part> parts = new ArrayList<>();
        try {
            int totalRows = countRows(invoiceXml);
            Transformer first = transformers.get();
            NodeInfo invoice = ((TransformerImpl) first).getConfiguration()
                    .buildDocumentTree(new StreamSource(new ByteArrayInputStream(invoiceXml))).getRootNode();

            int from = 1;
            int pagesSoFar = 0;
            while (true) {
                boolean tail = from + rowsPerChunk - 1 >= totalRows;
                String mode = from == 1 ? (tail ? "" : "head") : (tail ? "tail" : "middle");
                int to = tail ? 0 : from + rowsPerChunk - 1;

                Transformer transformer = stats.chunks == 0 ? first : transformers.get();
                transformer.setParameter("chunkMode", mode);
                transformer.setParameter("chunkFrom", from);
                transformer.setParameter("chunkTo", to);
                transformer.setParameter("chunkInitialPage", pagesSoFar + 1);
                File ifFile = renderToIntermediateFormat(transformer, invoice);
                stats.chunks++;

                PageScan scan = scanPages(ifFile);
                stats.pagesLaidOut += scan.pageCount;
                if (tail) {
                    parts.add(new Part(ifFile, scan.pageCount));
                    pagesSoFar += scan.pageCount;
                    stats.log.add(String.format("%s rows %d-%d: %d pages, keep all", mode, from, totalRows, scan.pageCount));
                    break;
                }
                int nextFrom = scan.firstRowOnLastPage;
                if (scan.pageCount < 2 || nextFrom <= from) {
                    throw new IllegalStateException("chunk too small to cut at a page break: rows " + from + "-" + to);
                }
                parts.add(new Part(ifFile, scan.pageCount - 1));
                pagesSoFar += scan.pageCount - 1;
                stats.log.add(String.format("%s rows %d-%d: %d pages, drop last (rows %d..), next from %d",
                        mode, from, to, scan.pageCount, nextFrom, nextFrom));
                from = nextFrom;
            }
            stats.pages = pagesSoFar;
            concatenate(parts, out);
            return stats;
        } catch (IOException | IFException | SAXException | javax.xml.stream.XMLStreamException
                 | javax.xml.parsers.ParserConfigurationException e) {
            throw new TransformerException(e);
        } finally {
            for (Part p : parts) {
                //noinspection ResultOfMethodCallIgnored
                p.file.delete();
            }
        }
    }

    private File renderToIntermediateFormat(Transformer transformer, NodeInfo invoice)
            throws IOException, FOPException, TransformerException {
        File ifFile = Files.createTempFile("ksef-chunk-", ".if.xml").toFile();
        FOUserAgent userAgent = factory.newFOUserAgent();
        IFDocumentHandler pdfHandler = factory.getRendererFactory().createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
        IFSerializer serializer = new IFSerializer(new IFContext(userAgent));
        serializer.mimicDocumentHandler(pdfHandler);
        userAgent.setDocumentHandlerOverride(serializer);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(ifFile))) {
            Fop fop = factory.newFop(MimeConstants.MIME_FOP_IF, userAgent, os);
            transformer.transform(invoice, new SAXResult(fop.getDefaultHandler()));
        }
        return ifFile;
    }

    private static class PageScan {
        int pageCount;
        int firstRowOnLastPage = Integer.MAX_VALUE;
    }

    /** Reads the IF once: page count and the first row id that starts on the last page. */
    private static PageScan scanPages(File ifFile) throws IOException, SAXException,
            javax.xml.parsers.ParserConfigurationException {
        final int[] currentPage = {-1};
        final List<int[]> rowPages = new ArrayList<>();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.newSAXParser().parse(ifFile, new DefaultHandler() {
            @Override
            public void startElement(String uri, String local, String qName, Attributes atts) {
                if (!IF_NS.equals(uri)) {
                    return;
                }
                if ("page".equals(local)) {
                    currentPage[0]++;
                } else if ("id".equals(local)) {
                    String name = atts.getValue("name");
                    if (name != null && name.startsWith(ROW_ID_PREFIX)) {
                        rowPages.add(new int[]{Integer.parseInt(name.substring(ROW_ID_PREFIX.length())), currentPage[0]});
                    }
                }
            }
        });
        PageScan scan = new PageScan();
        scan.pageCount = currentPage[0] + 1;
        for (int[] rp : rowPages) {
            if (rp[1] == scan.pageCount - 1) {
                scan.firstRowOnLastPage = Math.min(scan.firstRowOnLastPage, rp[0]);
            }
        }
        return scan;
    }

    private void concatenate(List<Part> parts, OutputStream out)
            throws IFException, IOException, TransformerException, SAXException,
            javax.xml.parsers.ParserConfigurationException {
        FOUserAgent userAgent = factory.newFOUserAgent();
        IFDocumentHandler pdfHandler = factory.getRendererFactory().createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
        pdfHandler.setResult(new StreamResult(out));
        org.apache.fop.render.intermediate.IFUtil.setupFonts(pdfHandler);
        IFConcatenator concatenator = new IFConcatenator(pdfHandler, null);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        for (Part part : parts) {
            XMLReader reader = spf.newSAXParser().getXMLReader();
            XMLFilterImpl dropTail = new DropPagesFilter(reader, part.keepPages);
            concatenator.appendDocument(new SAXSource(dropTail, new InputSource(part.file.toURI().toString())));
        }
        concatenator.finish();
    }

    /** Swallows every IF page element after the first {@code keepPages}. */
    private static class DropPagesFilter extends XMLFilterImpl {
        private final int keepPages;
        private int pageIndex = -1;
        private int skipDepth;

        DropPagesFilter(XMLReader parent, int keepPages) {
            super(parent);
            this.keepPages = keepPages;
        }

        @Override
        public void startElement(String uri, String local, String qName, Attributes atts) throws SAXException {
            if (skipDepth > 0) {
                skipDepth++;
                return;
            }
            if (IF_NS.equals(uri) && "page".equals(local) && ++pageIndex >= keepPages) {
                skipDepth = 1;
                return;
            }
            super.startElement(uri, local, qName, atts);
        }

        @Override
        public void endElement(String uri, String local, String qName) throws SAXException {
            if (skipDepth > 0) {
                skipDepth--;
                return;
            }
            super.endElement(uri, local, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (skipDepth == 0) {
                super.characters(ch, start, length);
            }
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            if (skipDepth == 0) {
                super.ignorableWhitespace(ch, start, length);
            }
        }
    }

    private static int countRows(byte[] invoiceXml) throws javax.xml.stream.XMLStreamException {
        XMLStreamReader r = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(invoiceXml));
        int depthFa = -1;
        int depth = 0;
        int count = 0;
        while (r.hasNext()) {
            int ev = r.next();
            if (ev == XMLStreamConstants.START_ELEMENT) {
                depth++;
                if ("Fa".equals(r.getLocalName()) && depthFa < 0) {
                    depthFa = depth;
                } else if ("FaWiersz".equals(r.getLocalName()) && depth == depthFa + 1) {
                    count++;
                }
            } else if (ev == XMLStreamConstants.END_ELEMENT) {
                if (depth == depthFa) {
                    depthFa = -1;
                }
                depth--;
            }
        }
        return count;
    }
}
