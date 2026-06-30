package io.alapierre.ksef.fop;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class PdfGeneratorConcurrencyTest {

    private static final Path INPUT_XML = Paths.get(
            "src/test/resources/faktury/fa3/podstawowa/FA_3_Przyklad_1.xml");

    @Test
    void sharedPdfGeneratorReusesFopFactoryForConcurrentInvoiceRendering() throws Exception {
        PdfGenerator generator = new PdfGenerator("fop.xconf");
        assertConcurrentRenderingKeepsConfiguredFonts(generator);
    }

    @Test
    void sharedPdfGeneratorCanUseConfiguredRendererPool() throws Exception {
        InvoicePdfConfig config = InvoicePdfConfig.builder()
                .rendererPoolSize(4)
                .build();
        PdfGenerator generator = new PdfGenerator("fop.xconf", config);

        assertConcurrentRenderingKeepsConfiguredFonts(generator);
    }

    private void assertConcurrentRenderingKeepsConfiguredFonts(PdfGenerator generator) throws Exception {
        byte[] invoiceXml = Files.readAllBytes(INPUT_XML);
        int threads = 16;
        int iterations = 120;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<RenderResult>> futures = new ArrayList<>();
            for (int i = 0; i < iterations; i++) {
                futures.add(executor.submit(() -> renderInvoice(generator, invoiceXml)));
            }

            List<RenderResult> results = new ArrayList<>();
            for (Future<RenderResult> future : futures) {
                results.add(future.get());
            }

            assertThat(results)
                    .hasSize(iterations)
                    .allSatisfy(result -> {
                        assertThat(result.pdfBytes).isGreaterThan(0);
                        assertThat(result.fonts)
                                .anySatisfy(font -> assertThat(font).contains("OpenSans"));
                    });
        } finally {
            executor.shutdownNow();
        }
    }

    private RenderResult renderInvoice(PdfGenerator generator, byte[] invoiceXml) throws Exception {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .languageLocale("pl")
                .showCorrectionDifferences(false)
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            generator.generateInvoice(invoiceXml, params, out);
            byte[] pdf = out.toByteArray();
            return new RenderResult(pdf.length, extractFonts(pdf));
        }
    }

    private Set<String> extractFonts(byte[] pdf) throws Exception {
        Set<String> fonts = new TreeSet<>();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;
                for (COSName fontName : resources.getFontNames()) {
                    PDFont font = resources.getFont(fontName);
                    if (font != null) {
                        fonts.add(font.getName());
                    }
                }
            }
        }
        return fonts;
    }

    private static class RenderResult {
        private final int pdfBytes;
        private final Set<String> fonts;

        private RenderResult(int pdfBytes, Set<String> fonts) {
            this.pdfBytes = pdfBytes;
            this.fonts = fonts;
        }
    }
}
