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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkedInvoiceRenderingTest {

    private static final String MULTI_TABLE_TEMPLATE = "templates/chunked/multi_table_invoice.xsl";
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
    void builtInTemplateIsRenderedInChunksWithTheSamePages() throws Exception {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder().schema(InvoiceSchema.FA3_1_0_E).build();

        assertChunkedLikeSinglePass(params);
    }

    @Test
    void tablesInSeveralPageSequencesAreRenderedInChunksWithTheSamePages() throws Exception {
        assertChunkedLikeSinglePass(paramsFor(MULTI_TABLE_TEMPLATE, Collections.<String, Object>emptyMap()));
    }

    @Test
    void rendersInSinglePassWhenTheDocumentRefersAcrossPages() throws Exception {
        InvoiceGenerationParams params = paramsFor(MULTI_TABLE_TEMPLATE,
                Collections.<String, Object>singletonMap("pageReference", "true"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChunkedRenderingResult result = generator.generateInvoiceChunked(longInvoice, params, out, ROWS_PER_CHUNK);

        assertFalse(result.isChunked());
        assertTrue(result.getSinglePassReason().contains("page-number-citation"), result.getSinglePassReason());
        assertEquals(pagesOf(singlePass(longInvoice, params)), pagesOf(out.toByteArray()));
    }

    @Test
    void rendersInSinglePassWhenTheDocumentFitsInOneChunk() throws Exception {
        InvoiceGenerationParams params = paramsFor(MULTI_TABLE_TEMPLATE, Collections.<String, Object>emptyMap());
        byte[] shortInvoice = invoiceWithRows(10);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChunkedRenderingResult result = generator.generateInvoiceChunked(shortInvoice, params, out, ROWS_PER_CHUNK);

        assertFalse(result.isChunked());
        assertEquals(pagesOf(singlePass(shortInvoice, params)), pagesOf(out.toByteArray()));
    }

    private static void assertChunkedLikeSinglePass(InvoiceGenerationParams params) throws Exception {
        ByteArrayOutputStream chunked = new ByteArrayOutputStream();
        ChunkedRenderingResult result = generator.generateInvoiceChunked(longInvoice, params, chunked, ROWS_PER_CHUNK);

        assertTrue(result.isChunked(), "expected chunked rendering, got: " + result.getSinglePassReason());
        assertTrue(result.getChunkCount() > 2, "chunks: " + result.getChunkCount());
        List<String> expected = pagesOf(singlePass(longInvoice, params));
        assertEquals(expected.size(), result.getPageCount());
        assertEquals(expected, pagesOf(chunked.toByteArray()));
    }

    private static InvoiceGenerationParams paramsFor(String templatePath, Map<String, Object> customProperties) {
        return InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .templatePath(templatePath)
                .customProperties(customProperties)
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
        int firstRow = invoice.indexOf("<FaWiersz>");
        String withoutRows = invoice.replaceAll("(?s)<FaWiersz>.*?</FaWiersz>\\s*", "");
        return (withoutRows.substring(0, firstRow) + positions + withoutRows.substring(firstRow))
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
