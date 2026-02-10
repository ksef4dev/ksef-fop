package io.alapierre.ksef.fop;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Helper class for tests that generate PDFs and need to extract text from them for assertions.
 */
class AbstractGeneratePdfTest {

    private static InputStream getResourceAsStream(String path) {
        return AbstractGeneratePdfTest.class.getClassLoader().getResourceAsStream(path);
    }

    private static InputStream getFopConfig() throws IOException {
        return getResourceAsStream("fop.xconf");
    }

    private String extractTextFromPdf(byte[] pdfData) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private byte[] generateFa3InvoicePdf(String invoiceResource) throws Exception {
        PdfGenerator generator = new PdfGenerator(getFopConfig());

        InvoiceGenerationParams invoiceGenerationParams = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .build();

        try (InputStream inputStream = getResourceAsStream(invoiceResource);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            assertNotNull(inputStream);

            generator.generateInvoice(IOUtils.toByteArray(inputStream), invoiceGenerationParams, outputStream);

            return outputStream.toByteArray();
        }
    }

    String generateFa3InvoiceText(String invoiceResource) throws Exception {
        return extractTextFromPdf(generateFa3InvoicePdf(invoiceResource));
    }

    static String textResource(String resource) throws IOException {
        try (InputStream inputStream = getResourceAsStream(resource)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
