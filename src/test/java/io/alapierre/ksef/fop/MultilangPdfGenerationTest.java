package io.alapierre.ksef.fop;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;

@Slf4j
class MultilangPdfGenerationTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void generatePlPdf() throws Exception {
        generatePdf(Language.PL,
                "src/test/resources/faktury/fa3/podstawowa/FA_3_Przyklad_1.xml",
                "src/test/resources/invoice_fa3_vat_pl.pdf");
        generatePdf(Language.PL,
                "src/test/resources/faktury/fa3/korygujaca/FA_3_Przyklad_1.xml",
                "src/test/resources/invoice_fa3_kor_pl.pdf");
        generatePdf(Language.PL,
                "src/test/resources/faktury/fa3/zaliczkowa/FA_3_Przyklad_1.xml",
                "src/test/resources/invoice_fa3_zal_pl.pdf");
        generatePdf(Language.PL,
                "src/test/resources/faktury/fa3/rozliczeniowa/FA_3_Przyklad_1.xml",
                "src/test/resources/invoice_fa3_roz_pl.pdf");
    }

    @Test
    void generateEnPdf() throws Exception {
        generatePdf(Language.EN,
                "src/test/resources/faktury/fa3/podstawowa/FA_3_Przyklad_1.xml",
                "src/test/resources/invoice_fa3_vat_en.pdf");
        generatePdf(Language.EN,
                "src/test/resources/faktury/fa3/korygujaca/FA_3_Przyklad_1.xml",
                "src/test/resources/invoice_fa3_kor_en.pdf");
        generatePdf(Language.EN,
                "src/test/resources/faktury/fa3/zaliczkowa/FA_3_Przyklad_1.xml",
                "src/test/resources/invoice_fa3_zal_en.pdf");
        generatePdf(Language.EN,
                "src/test/resources/faktury/fa3/rozliczeniowa/FA_3_Przyklad_1.xml",
                "src/test/resources/invoice_fa3_roz_en.pdf");
//        generatePdf(Language.EN,
//                "src/test/resources/faktury/fa3/rozliczeniowa_korekta/FA_3_Przyklad_1.xml",
//                "src/test/resources/invoice_fa3_roz_kor_en.pdf");
//        generatePdf(Language.EN,
//                "src/test/resources/faktury/fa3/zaliczkowa_korekta/FA_3_Przyklad_1.xml",
//                "src/test/resources/invoice_fa3_zal_kor_en.pdf");
    }

    private void generatePdf(Language lang,
                             String invoicePath,
                             String outputPath) throws Exception {
        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath))) {

            byte[] invoiceXml = Files.readAllBytes(Paths.get(invoicePath));

            InvoiceGenerationParams invoiceGenerationParams = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(lang)
                    .build();
            
            generator.generateInvoice(invoiceXml, invoiceGenerationParams, out);
        }
    }
}

