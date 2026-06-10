package io.alapierre.ksef.fop;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.net.URI;
import java.util.Collections;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests HTTP-based template loading in three modes.
 *
 * <h2>Catalog mode (always runs)</h2>
 * {@link #generateInvoice_templatePathIsHttpUrlMappedInCatalog()} – URL is allowlisted in
 * {@code catalog.xml} and rewritten to a classpath file.  No network connection is made;
 * template-server can be down.
 *
 * <h2>Live HTTP – root path (opt-in)</h2>
 * {@link #generateInvoice_liveHttpFetchFromTemplateServer()} – fetches
 * {@code /xslt/ksef_invoice} and all relative siblings from template-server.
 *
 * <h2>Live HTTP – NIP-parameterised path (opt-in)</h2>
 * {@link #generateInvoice_liveHttpFetchWithNipInPath()} – the key test:
 * <ol>
 *   <li>{@code templatePath = "http://…/xslt/{nip}/ksef_invoice"}</li>
 *   <li>{@code resourceRoot = URI.create("http://…/xslt")}</li>
 *   <li>Relative includes inside the XSL (e.g. {@code href="invoice-rows.xsl"}) are resolved
 *       by Saxon against the parent system-id, producing
 *       {@code http://…/xslt/{nip}/invoice-rows.xsl}.</li>
 *   <li>That URL is under {@code remoteBaseUrl}, so {@code TemplateResolver} fetches it via
 *       HTTP → template-server sees {@code GET /xslt/{nip}/invoice-rows} and falls back to
 *       the root {@code xslt/} folder when no per-NIP override exists.</li>
 * </ol>
 * Enable the live tests when {@code template-server} is running on port 8077:
 * <pre>{@code
 *   mvn test -Dtest=HttpTemplatePathInvoiceTest \
 *             -Dtemplate.server.running=true
 * }</pre>
 */
class HttpTemplatePathInvoiceTest {

    private static final String TEMPLATE_SERVER_BASE = "http://localhost:8077/xslt";

    /** NIP of Podmiot1 (seller) in FA_3_Przyklad_1.xml – used as the tenant segment. */
    private static final String SELLER_NIP = "6891152920";

    private static final String TEMPLATE_ROOT_URL        = TEMPLATE_SERVER_BASE + "/ksef_invoice";
    private static final String TEMPLATE_NIP_URL         = TEMPLATE_SERVER_BASE + "/" + SELLER_NIP + "/ksef_invoice";

    private static final Path OUTPUT_PDF_CATALOG = Paths.get("target/http-template-path-invoice-catalog.pdf");
    private static final Path OUTPUT_PDF_LIVE    = Paths.get("target/http-template-path-invoice-live.pdf");
    private static final Path OUTPUT_PDF_NIP     = Paths.get("target/http-template-path-invoice-nip.pdf");

    // -----------------------------------------------------------------------
    // Catalog-backed (always runs, server not needed)
    // -----------------------------------------------------------------------

    /**
     * URL is allowlisted in {@code catalog.xml} → classpath stylesheet, zero network I/O.
     */
    @Test
    void generateInvoice_templatePathIsHttpUrlMappedInCatalog() throws Exception {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .ksefNumber("TEST-HTTP-TEMPLATE-KSEF-NR")
                .templatePath(TEMPLATE_ROOT_URL)
                .build();

        String text = generateAndExtractText(params, OUTPUT_PDF_CATALOG);

        assertTrue(text.contains("TEST-HTTP-TEMPLATE-KSEF-NR"), "Expected KSeF number from params in PDF text");
        assertTrue(text.contains("CeDeE"), "Expected sample buyer name from FA_3_Przyklad_1.xml in PDF");
    }

    // -----------------------------------------------------------------------
    // Live HTTP – root path  (requires template-server on localhost:8077)
    // -----------------------------------------------------------------------

    @Test
    @EnabledIfSystemProperty(named = "template.server.running", matches = "true")
    void generateInvoice_liveHttpFetchFromTemplateServer() throws Exception {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .ksefNumber("TEST-LIVE-HTTP-KSEF-NR")
                .templatePath(TEMPLATE_ROOT_URL)
                .resourceRoot(URI.create(TEMPLATE_SERVER_BASE))
                .build();

        String text = generateAndExtractText(params, OUTPUT_PDF_LIVE);

        assertTrue(text.contains("TEST-LIVE-HTTP-KSEF-NR"), "Expected KSeF number in PDF text");
        assertTrue(text.contains("CeDeE"), "Expected sample buyer name in PDF text");
    }

    // -----------------------------------------------------------------------
    // Live HTTP – NIP in path  (requires template-server on localhost:8077)
    // -----------------------------------------------------------------------

    /**
     * The main stylesheet is loaded from {@code /xslt/{nip}/ksef_invoice}.
     * Because includes inside the XSL are relative (e.g. {@code href="invoice-rows.xsl"}),
     * Saxon resolves them against the parent system-id, producing URLs that contain the NIP.
     * {@link io.alapierre.ksef.fop.internal.TemplateResolver} fetches all of them from
     * template-server, which falls back to the root {@code xslt/} folder when no per-NIP
     * override exists.
     */
    @Test
    @EnabledIfSystemProperty(named = "template.server.running", matches = "true")
    void generateInvoice_liveHttpFetchWithNipInPath() throws Exception {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .ksefNumber("TEST-NIP-IN-PATH-KSEF-NR")
                .templatePath(TEMPLATE_NIP_URL)                  // …/xslt/6891152920/ksef_invoice
                .resourceRoot(URI.create(TEMPLATE_SERVER_BASE))  // …/xslt  (no NIP here!)
                .customProperties(Collections.singletonMap("sellerNip", SELLER_NIP))
                .build();

        String text = generateAndExtractText(params, OUTPUT_PDF_NIP);

        assertTrue(text.contains("TEST-NIP-IN-PATH-KSEF-NR"), "Expected KSeF number in PDF text");
        assertTrue(text.contains("CeDeE"), "Expected sample buyer name in PDF text");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String generateAndExtractText(InvoiceGenerationParams params, Path outputPdf) throws Exception {
        try (InputStream fopCfg = resource("fop.xconf");
             InputStream invoiceIs = resource("faktury/fa3/podstawowa/FA_3_Przyklad_1.xml")) {
            assertNotNull(fopCfg);
            assertNotNull(invoiceIs);

            PdfGenerator generator = new PdfGenerator(fopCfg);
            byte[] invoiceXml = IOUtils.toByteArray(invoiceIs);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            generator.generateInvoice(invoiceXml, params, out);

            byte[] pdf = out.toByteArray();
            assertTrue(pdf.length > 1024, "Generated PDF unexpectedly small");

            Files.createDirectories(outputPdf.getParent());
            Files.write(outputPdf, pdf);

            return extractText(pdf);
        }
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static InputStream resource(String path) {
        return HttpTemplatePathInvoiceTest.class.getClassLoader().getResourceAsStream(path);
    }
}
