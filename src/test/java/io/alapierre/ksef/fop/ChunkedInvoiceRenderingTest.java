package io.alapierre.ksef.fop;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkedInvoiceRenderingTest {

    private static final String CHUNKED_TEMPLATE = "templates/chunked/chunked_invoice.xsl";
    private static final int ROWS = 300;
    private static final int ROWS_PER_CHUNK = 40;

    private static PdfGenerator generator;
    private static byte[] longInvoice;

    @BeforeAll
    static void setUp() throws Exception {
        try (InputStream fopConfig = resource("fop.xconf")) {
            generator = new PdfGenerator(fopConfig);
        }
        longInvoice = invoiceWithRows(ROWS);
    }

    @Test
    void chunkedPdfHasTheSamePagesAsSinglePassPdf() throws Exception {
        InvoiceGenerationParams params = paramsFor(CHUNKED_TEMPLATE);

        ByteArrayOutputStream chunked = new ByteArrayOutputStream();
        ChunkedRenderingResult result = generator.generateInvoiceChunked(longInvoice, params, chunked, ROWS_PER_CHUNK);

        assertTrue(result.isChunked(), "expected chunked rendering, got: " + result.getSinglePassReason());
        assertTrue(result.getChunkCount() > 1);
        List<String> expectedPages = pagesOf(singlePass(longInvoice, params));
        List<String> chunkedPages = pagesOf(chunked.toByteArray());
        assertEquals(expectedPages.size(), result.getPageCount());
        assertEquals(expectedPages, chunkedPages);
    }

    @Test
    void cutsOnlyAtPageBreaksWithoutPartiallyFilledPages() throws Exception {
        ByteArrayOutputStream chunked = new ByteArrayOutputStream();
        generator.generateInvoiceChunked(longInvoice, paramsFor(CHUNKED_TEMPLATE), chunked, ROWS_PER_CHUNK);

        List<String> pages = pagesOf(chunked.toByteArray());
        assertTrue(pages.get(0).contains("INVOICE HEADER"));
        assertTrue(pages.get(0).contains("FIRST PAGE FOOTER"));
        for (int i = 1; i < pages.size(); i++) {
            assertFalse(pages.get(i).contains("FIRST PAGE FOOTER"), "continuation page " + (i + 1));
            assertTrue(pages.get(i).contains("page " + (i + 1)), "page numbering continues on page " + (i + 1));
        }
        assertEquals(ROWS, positionsIn(pages), "every position is rendered exactly once");
        assertTrue(pages.get(pages.size() - 1).contains("INVOICE SUMMARY: " + ROWS + " positions"));
    }

    @Test
    void rendersInSinglePassWhenTemplateDoesNotSupportChunks() throws Exception {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder().schema(InvoiceSchema.FA3_1_0_E).build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChunkedRenderingResult result = generator.generateInvoiceChunked(longInvoice, params, out, ROWS_PER_CHUNK);

        assertFalse(result.isChunked());
        assertTrue(result.getSinglePassReason().contains("does not support"));
        assertEquals(pagesOf(singlePass(longInvoice, params)), pagesOf(out.toByteArray()));
    }

    @Test
    void rendersInSinglePassWhenInvoiceFitsInOneChunk() throws Exception {
        InvoiceGenerationParams params = paramsFor(CHUNKED_TEMPLATE);
        byte[] shortInvoice = invoiceWithRows(ROWS_PER_CHUNK);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChunkedRenderingResult result = generator.generateInvoiceChunked(shortInvoice, params, out, ROWS_PER_CHUNK);

        assertFalse(result.isChunked());
        assertEquals(pagesOf(singlePass(shortInvoice, params)), pagesOf(out.toByteArray()));
    }

    private static InvoiceGenerationParams paramsFor(String templatePath) {
        return InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .templatePath(templatePath)
                .build();
    }

    private static byte[] singlePass(byte[] invoice, InvoiceGenerationParams params) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateInvoice(invoice, params, out);
        return out.toByteArray();
    }

    private static List<String> pagesOf(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            List<String> pages = new ArrayList<>();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                pages.add(stripper.getText(document));
            }
            return pages;
        }
    }

    private static int positionsIn(List<String> pages) {
        Pattern position = Pattern.compile("POSITION-(\\d+) ");
        int count = 0;
        for (String page : pages) {
            Matcher matcher = position.matcher(page);
            while (matcher.find()) {
                count++;
            }
        }
        return count;
    }

    /** Sample FA(3) invoice with its single position repeated; names of varying length give varying row heights. */
    private static byte[] invoiceWithRows(int rows) throws IOException {
        String invoice;
        try (InputStream in = resource("faktury/fa3/podstawowa/FA_3_Przyklad_1.xml")) {
            assertNotNull(in);
            invoice = new String(IOUtils.toByteArray(in), StandardCharsets.UTF_8);
        }
        Matcher row = Pattern.compile("<FaWiersz>.*?</FaWiersz>", Pattern.DOTALL).matcher(invoice);
        assertTrue(row.find());
        String template = row.group();
        StringBuilder positions = new StringBuilder();
        for (int i = 1; i <= rows; i++) {
            String name = "POSITION-" + i + " " + repeat("long product description ", i % 7);
            positions.append(template
                    .replaceFirst("<NrWierszaFa>[^<]*</NrWierszaFa>", "<NrWierszaFa>" + i + "</NrWierszaFa>")
                    .replaceFirst("<P_7>[^<]*</P_7>", "<P_7>" + name + "</P_7>"));
        }
        String withoutRows = invoice.replaceAll("(?s)<FaWiersz>.*?</FaWiersz>\\s*", "");
        int insertAt = withoutRows.indexOf("<Rozliczenie>") >= 0 ? withoutRows.indexOf("<Rozliczenie>")
                : withoutRows.indexOf("<Platnosc>");
        return (withoutRows.substring(0, insertAt) + positions + withoutRows.substring(insertAt))
                .getBytes(StandardCharsets.UTF_8);
    }

    private static String repeat(String text, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

    private static InputStream resource(String path) {
        return ChunkedInvoiceRenderingTest.class.getClassLoader().getResourceAsStream(path);
    }
}
