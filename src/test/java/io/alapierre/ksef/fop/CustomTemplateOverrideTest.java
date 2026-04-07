package io.alapierre.ksef.fop;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomTemplateOverrideTest {

    private static InputStream getResourceAsStream(String path) {
        return CustomTemplateOverrideTest.class.getClassLoader().getResourceAsStream(path);
    }

    private static String extractTextFromPdf(byte[] pdfData) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            return new PDFTextStripper().getText(document);
        }
    }

    @Test
    void generateInvoice_usesProvidedTemplatePath() throws Exception {
        try (InputStream fopCfg = getResourceAsStream("fop.xconf")) {
            assertNotNull(fopCfg);
            PdfGenerator generator = new PdfGenerator(fopCfg);

            try (InputStream invoiceIs = getResourceAsStream("faktury/fa3/podstawowa/FA_3_Przyklad_1.xml")) {
                assertNotNull(invoiceIs);
                byte[] invoiceXml = IOUtils.toByteArray(invoiceIs);

                InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                        .schema(InvoiceSchema.FA3_1_0_E)
                        .ksefNumber("TEST-KSEF-NUMBER")
                        .templatePath("templates/custom/custom_invoice.xsl")
                        .customProperties(Collections.singletonMap("customPropertyDemo", "HELLO-CUSTOM-PROPERTY"))
                        .build();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                generator.generateInvoice(invoiceXml, params, out);

                String text = extractTextFromPdf(out.toByteArray());
                assertTrue(text.contains("CUSTOM_TEMPLATE_MARKER"), "Expected marker from custom template in PDF text");
                assertTrue(text.contains("nrKsef=TEST-KSEF-NUMBER"), "Expected nrKsef parameter rendered by custom template");
                assertTrue(text.contains("customPropertyDemo=HELLO-CUSTOM-PROPERTY"), "Expected custom property rendered by custom template");
            }
        }
    }
}

