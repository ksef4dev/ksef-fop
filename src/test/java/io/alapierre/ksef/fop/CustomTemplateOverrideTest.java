package io.alapierre.ksef.fop;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
                Path customTemplateRoot = createCustomTemplateRoot();

                InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                        .schema(InvoiceSchema.FA3_1_0_E)
                        .ksefNumber("TEST-KSEF-NUMBER")
                        .templatePath("templates/custom/custom_invoice.xsl")
                        .templateRoots(Collections.singletonList(customTemplateRoot))
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

    @Test
    void generateInvoice_failsWhenTemplatePathIsNotAnExistingLocalFile() throws Exception {
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
                        .build();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Exception ex = assertThrows(Exception.class, () -> generator.generateInvoice(invoiceXml, params, out));
                assertTrue(ex.getMessage().contains("Cannot resolve template resource"));
            }
        }
    }

    @Test
    void generateInvoice_fallsBackToClasspathForMissingImportedTemplate() throws Exception {
        try (InputStream fopCfg = getResourceAsStream("fop.xconf")) {
            assertNotNull(fopCfg);
            PdfGenerator generator = new PdfGenerator(fopCfg);

            try (InputStream invoiceIs = getResourceAsStream("faktury/fa3/podstawowa/FA_3_Przyklad_1.xml")) {
                assertNotNull(invoiceIs);
                byte[] invoiceXml = IOUtils.toByteArray(invoiceIs);
                Path customRoot = createPartialOverrideTemplateRoot();

                InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                        .schema(InvoiceSchema.FA3_1_0_E)
                        .templatePath("templates/fa3/ksef_invoice.xsl")
                        .templateRoots(Collections.singletonList(customRoot))
                        .build();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                generator.generateInvoice(invoiceXml, params, out);

                String text = extractTextFromPdf(out.toByteArray());
                assertTrue(text.contains("KOD"), "Expected content from classpath imported templates");
            }
        }
    }

    private Path createCustomTemplateRoot() throws IOException {
        try (InputStream customTemplate = getResourceAsStream("templates/custom/custom_invoice.xsl")) {
            assertNotNull(customTemplate);
            Path root = Files.createTempDirectory("ksef-custom-template-root-");
            Path templateDir = root.resolve("templates/custom");
            Files.createDirectories(templateDir);
            Path templateFile = templateDir.resolve("custom_invoice.xsl");
            Files.copy(customTemplate, templateFile, StandardCopyOption.REPLACE_EXISTING);
            root.toFile().deleteOnExit();
            templateFile.toFile().deleteOnExit();
            return root;
        }
    }

    private Path createPartialOverrideTemplateRoot() throws IOException {
        try (InputStream builtInTemplate = getResourceAsStream("templates/fa3/ksef_invoice.xsl")) {
            assertNotNull(builtInTemplate);
            Path root = Files.createTempDirectory("ksef-partial-template-root-");
            Path templateDir = root.resolve("templates/fa3");
            Files.createDirectories(templateDir);
            Path templateFile = templateDir.resolve("ksef_invoice.xsl");
            Files.copy(builtInTemplate, templateFile, StandardCopyOption.REPLACE_EXISTING);
            root.toFile().deleteOnExit();
            templateFile.toFile().deleteOnExit();
            return root;
        }
    }
}

