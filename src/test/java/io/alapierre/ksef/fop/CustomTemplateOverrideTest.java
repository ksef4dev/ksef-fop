package io.alapierre.ksef.fop;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomTemplateOverrideTest {

    private static final String FILESYSTEM_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<xsl:stylesheet version=\"1.0\"\n"
            + "                xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n"
            + "                xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
            + "    <xsl:param name=\"labels\"/>\n"
            + "    <xsl:param name=\"nrKsef\"/>\n"
            + "    <xsl:param name=\"issuerUser\"/>\n"
            + "    <xsl:param name=\"showFooter\"/>\n"
            + "    <xsl:param name=\"useExtendedDecimalPlaces\"/>\n"
            + "    <xsl:param name=\"showCorrectionDifferences\"/>\n"
            + "    <xsl:param name=\"duplicateDate\"/>\n"
            + "    <xsl:param name=\"currencyDate\"/>\n"
            + "    <xsl:param name=\"qrCodesCount\"/>\n"
            + "    <xsl:param name=\"customPropertyDemo\"/>\n"
            + "    <xsl:template match=\"/\">\n"
            + "        <fo:root>\n"
            + "            <fo:layout-master-set>\n"
            + "                <fo:simple-page-master master-name=\"A4\"\n"
            + "                                       page-height=\"29.7cm\"\n"
            + "                                       page-width=\"21cm\"\n"
            + "                                       margin=\"1cm\">\n"
            + "                    <fo:region-body/>\n"
            + "                </fo:simple-page-master>\n"
            + "            </fo:layout-master-set>\n"
            + "            <fo:page-sequence master-reference=\"A4\">\n"
            + "                <fo:flow flow-name=\"xsl-region-body\">\n"
            + "                    <fo:block font-size=\"12pt\" font-family=\"Helvetica\">\n"
            + "                        FILESYSTEM_TEMPLATE_MARKER\n"
            + "                    </fo:block>\n"
            + "                    <fo:block font-size=\"10pt\" font-family=\"Helvetica\">\n"
            + "                        <xsl:text>nrKsef=</xsl:text>\n"
            + "                        <xsl:value-of select=\"$nrKsef\"/>\n"
            + "                    </fo:block>\n"
            + "                    <fo:block font-size=\"10pt\" font-family=\"Helvetica\">\n"
            + "                        <xsl:text>customPropertyDemo=</xsl:text>\n"
            + "                        <xsl:value-of select=\"$customPropertyDemo\"/>\n"
            + "                    </fo:block>\n"
            + "                </fo:flow>\n"
            + "            </fo:page-sequence>\n"
            + "        </fo:root>\n"
            + "    </xsl:template>\n"
            + "</xsl:stylesheet>\n";

    @Test
    void generateInvoice_usesClasspathTemplatePath() throws Exception {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .ksefNumber("TEST-KSEF-NUMBER")
                .templatePath("templates/custom/custom_invoice.xsl")
                .customProperties(Collections.singletonMap("customPropertyDemo", "HELLO-CUSTOM-PROPERTY"))
                .build();

        String text = generateAndExtractText(params);

        assertTrue(text.contains("CUSTOM_TEMPLATE_MARKER"), "Expected classpath template marker in PDF");
        assertTrue(text.contains("nrKsef=TEST-KSEF-NUMBER"), "Expected nrKsef parameter in PDF");
        assertTrue(text.contains("customPropertyDemo=HELLO-CUSTOM-PROPERTY"), "Expected custom property in PDF");
    }

    @Test
    void generateInvoice_usesFilesystemTemplateRoot(@TempDir Path tempDir) throws Exception {
        Path templateDir = tempDir.resolve("templates/custom");
        Files.createDirectories(templateDir);
        Files.write(templateDir.resolve("custom_invoice.xsl"),
                FILESYSTEM_TEMPLATE.getBytes(StandardCharsets.UTF_8));

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .ksefNumber("FS-KSEF-NUMBER")
                .templatePath("templates/custom/custom_invoice.xsl")
                .customProperties(Collections.singletonMap("customPropertyDemo", "HELLO-FS-PROPERTY"))
                .templateRoot(tempDir)
                .build();

        String text = generateAndExtractText(params);

        assertTrue(text.contains("FILESYSTEM_TEMPLATE_MARKER"), "Expected filesystem template marker in PDF");
        assertTrue(text.contains("nrKsef=FS-KSEF-NUMBER"), "Expected nrKsef parameter in PDF");
        assertTrue(text.contains("customPropertyDemo=HELLO-FS-PROPERTY"), "Expected custom property in PDF");
    }

    @Test
    void generateInvoice_filesystemRootShadowsClasspath(@TempDir Path tempDir) throws Exception {
        Path templateDir = tempDir.resolve("templates/custom");
        Files.createDirectories(templateDir);
        Files.write(templateDir.resolve("custom_invoice.xsl"),
                FILESYSTEM_TEMPLATE.getBytes(StandardCharsets.UTF_8));

        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .ksefNumber("SHADOW-TEST")
                .templatePath("templates/custom/custom_invoice.xsl")
                .templateRoot(tempDir)
                .build();

        String text = generateAndExtractText(params);

        assertTrue(text.contains("FILESYSTEM_TEMPLATE_MARKER"), "Filesystem root must shadow classpath");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String generateAndExtractText(InvoiceGenerationParams params) throws Exception {
        try (InputStream fopCfg = resource("fop.xconf");
             InputStream invoiceIs = resource("faktury/fa3/podstawowa/FA_3_Przyklad_1.xml")) {
            assertNotNull(fopCfg);
            assertNotNull(invoiceIs);

            PdfGenerator generator = new PdfGenerator(fopCfg);
            byte[] invoiceXml = IOUtils.toByteArray(invoiceIs);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            generator.generateInvoice(invoiceXml, params, out);

            return extractText(out.toByteArray());
        }
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static InputStream resource(String path) {
        return CustomTemplateOverrideTest.class.getClassLoader().getResourceAsStream(path);
    }
}
