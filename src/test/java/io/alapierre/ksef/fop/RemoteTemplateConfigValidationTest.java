package io.alapierre.ksef.fop;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Early-fail checks for HTTP {@code resourceRoot} / {@code templatePath} consistency.
 */
class RemoteTemplateConfigValidationTest {

    private static final String TEMPLATE_SERVER_BASE = "http://localhost:8077/xslt";

    @Test
    void invalidHttpResourceRootFailsBeforeNetworkFetch() {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .templatePath(TEMPLATE_SERVER_BASE + "/ksef_invoice")
                .resourceRoot(URI.create("ftp://localhost/xslt"))
                .build();

        assertThatThrownBy(() -> generate(params))
                .isInstanceOf(javax.xml.transform.TransformerException.class)
                .hasMessageContaining("http(s) URL");
    }

    @Test
    void httpTemplatePathOutsideResourceRootFailsBeforeNetworkFetch() {
        InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                .schema(InvoiceSchema.FA3_1_0_E)
                .templatePath("http://other-host:8077/xslt/ksef_invoice")
                .resourceRoot(URI.create(TEMPLATE_SERVER_BASE))
                .build();

        assertThatThrownBy(() -> generate(params))
                .isInstanceOf(javax.xml.transform.TransformerException.class)
                .hasMessageContaining("not under any configured HTTP resource root");
    }

    private static void generate(InvoiceGenerationParams params) throws Exception {
        try (InputStream fopCfg = resource("fop.xconf");
             InputStream invoiceIs = resource("faktury/fa3/podstawowa/FA_3_Przyklad_1.xml")) {
            PdfGenerator generator = new PdfGenerator(fopCfg);
            byte[] invoiceXml = IOUtils.toByteArray(invoiceIs);
            generator.generateInvoice(invoiceXml, params, new ByteArrayOutputStream());
        }
    }

    private static InputStream resource(String path) {
        return RemoteTemplateConfigValidationTest.class.getClassLoader().getResourceAsStream(path);
    }
}
