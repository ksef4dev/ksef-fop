package io.alapierre.ksef.fop;

import lombok.extern.slf4j.Slf4j;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;


/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.11
 */
@Slf4j
class GeneratePdfTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void genV3UpoByService() throws Exception {

        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo-v3-service.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC.xml");
            Source src = new StreamSource(xml);

            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V3)
                    .build();

            generator.generateUpo(src, params, out);
        }
    }

    @Test
    void genV3UpoWithConfFromClasspath() throws Exception {

        PdfGenerator generator = new PdfGenerator("fop.xconf");

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo-v3-classpath.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC.xml");
            Source src = new StreamSource(xml);

            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V3)
                    .build();

            generator.generateUpo(src, params, out);
        }
    }

    @Test
    void genV4_2UpoByService() throws Exception {

        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo-v4-2-service.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC-v4-2.xml");
            Source src = new StreamSource(xml);

            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_2)
                    .build();

            generator.generateUpo(src, params, out);
        }
    }

    @Test
    void genV4_2UpoWithConfFromClasspath() throws Exception {

        PdfGenerator generator = new PdfGenerator("fop.xconf");

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo-v4-2-classpath.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC-v4-2.xml");
            Source src = new StreamSource(xml);

            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_2)
                    .build();

            generator.generateUpo(src, params, out);
        }
    }

    @Test
    void genV4_3UpoByService() throws Exception {

        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo-v4-3-service.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC-v4-3.xml");
            Source src = new StreamSource(xml);

            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .build();

            generator.generateUpo(src, params, out);
        }
    }

    @Test
    void gen() throws Exception {

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo.pdf"))) {

            FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            Configuration cfg = cfgBuilder.buildFromFile(new File("src/test/resources/fop.xconf"));
            builder.setConfiguration(cfg);
            FopFactory fopFactory = builder.build();
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource("src/main/resources/templates/upo_v3/ksef_upo.fop"));

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC.xml");
            Source src = new StreamSource(xml);
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        }
    }

    @Test
    void generateFa2InvoicePdf() throws Exception {
        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/invoice.pdf"))) {

            byte[] invoiceXml = Files.readAllBytes(Paths.get("src/test/resources/faktury/fa2/korygujaca/FA_2_Przyklad_2.xml"));

            InvoiceGenerationParams invoiceGenerationParams = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA2_1_0_E)
                    .build();
            generator.generateInvoice(invoiceXml, invoiceGenerationParams, out);
        }
    }

    @Test
    void generateFa3InvoicePdf() throws Exception {
        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/invoice_fa3.pdf"))) {

            byte[] invoiceXml = Files.readAllBytes(Paths.get("src/test/resources/faktury/fa3/podstawowa/FA_3_Przyklad_WZ.xml"));

            String verificationLink = "https://qr-test.ksef.mf.gov.pl/invoice/5451824119/31-01-2026/KxwNsNKtYSXLfcVsRnXAANUXT6NepXk42xOXUXaF8xE";
            InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(verificationLink);
            InvoiceGenerationParams invoiceGenerationParams = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .ksefNumber("5451824119-20260131-0200206A4F2C-92")
                    .invoiceQRCodeGeneratorRequest(invoiceQRCodeGeneratorRequest)
                    .build();
            generator.generateInvoice(invoiceXml, invoiceGenerationParams, out);
        }
    }

    @Test
    void generateFa3InvoicePdfNonUE() throws Exception {
        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out =
                     new BufferedOutputStream(new FileOutputStream("src/test/resources/invoice_non_ue.pdf"))) {

            byte[] invoiceXml = Files.readAllBytes(Paths.get("src/test/resources/faktury/fa3/poza_ue/FA_3_Przyklad_23.xml"));

            InvoiceGenerationParams invoiceGenerationParams = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .build();
            generator.generateInvoice(invoiceXml, invoiceGenerationParams, out);
        }
    }

    @Test
    void generateInvoicePdfWithAdditionalData() throws Exception {
        String ksefNumber = "6891152920-20251008-010000B4CF64-9C";
        String verificationLink = "https://ksef-test.mf.gov.pl/web/verify/6891152920-20231221-B3242FB4B54B-DF/ssTckvmMFEeA3vp589ExHzTRVhbDksjcFzKoXi4K%2F%2F0%3D";
        File logoFile = new File("src/test/resources/Logo.svg");
        byte[] logo = Files.readAllBytes(logoFile.toPath());

        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/invoice.pdf"))) {

            byte[] invoiceXml = Files.readAllBytes(Paths.get("src/test/resources/faktury/fa2/podstawowa/FA_2_Przyklad_20.xml"));

            InvoiceGenerationParams invoiceGenerationParams = InvoiceGenerationParams.builder()
                    .ksefNumber(ksefNumber)
                    .verificationLink(verificationLink)
                    .logo(logo)
                    .schema(InvoiceSchema.FA2_1_0_E)
                    .build();

            generator.generateInvoice(invoiceXml, invoiceGenerationParams, out);
        }
    }

    @Test
    void testInvoicePdfGenerateFromExampleInvoices() throws Exception {
        String ksefNumber = "6891152920-20231221-B3242FB4B54B-DF";
        String verificationLink = "https://ksef-test.mf.gov.pl/web/verify/6891152920-20231221-B3242FB4B54B-DF/ssTckvmMFEeA3vp589ExHzTRVhbDksjcFzKoXi4K%2F%2F0%3D";
        File qrCodeFile = new File("src/test/resources/barcode.png");
        byte[] qrCode = Files.readAllBytes(qrCodeFile.toPath());
        File logoFile = new File("src/test/resources/Logo.svg");
        byte[] logo = Files.readAllBytes(logoFile.toPath());
        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        Path fa2InvoiceFolder = Paths.get("src/test/resources/faktury/fa2/podstawowa");
        testForFolder(fa2InvoiceFolder, ksefNumber, verificationLink, false, qrCode, logo, InvoiceSchema.FA2_1_0_E, generator);

        Path fa3InvoiceFolder = Paths.get("src/test/resources/faktury/fa3/podstawowa");
        testForFolder(fa3InvoiceFolder, ksefNumber, verificationLink, false, qrCode, logo, InvoiceSchema.FA3_1_0_E, generator);


        Path zaliczkowaInvoiceFolder = Paths.get("src/test/resources/faktury/fa2/zaliczkowa");
        testForFolder(zaliczkowaInvoiceFolder, ksefNumber, verificationLink, false, qrCode, logo, InvoiceSchema.FA2_1_0_E, generator);

        Path rozliczeniowaInvoiceFolder = Paths.get("src/test/resources/faktury/fa2/rozliczeniowa");
        testForFolder(rozliczeniowaInvoiceFolder, ksefNumber, verificationLink, false, qrCode, logo, InvoiceSchema.FA2_1_0_E, generator);

        Path correctionFolder = Paths.get("src/test/resources/faktury/fa2/korygujaca");
        testForFolder(correctionFolder, ksefNumber, verificationLink, false, qrCode, logo, InvoiceSchema.FA2_1_0_E, generator);

        Path correctionFolderWithCorrectionDifferences = Paths.get("src/test/resources/faktury/fa2/korygujaca");
        testForFolder(correctionFolderWithCorrectionDifferences, ksefNumber, verificationLink, true, qrCode, logo, InvoiceSchema.FA2_1_0_E, generator);



    }

    private void testForFolder(Path invoiceFolder,
                               String ksefNumber,
                               String verificationLink,
                               boolean showCorrectionDifferences,
                               byte[] qrCode,
                               byte[] logo,
                               InvoiceSchema schema,
                               PdfGenerator generator) throws Exception {
        // Pobieranie wszystkich plików XML z folderu
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(invoiceFolder, "*.xml")) {
            for (Path entry : stream) {
                // Tworzenie strumienia dla każdego pliku XML
                try (OutputStream out = new BufferedOutputStream(new ByteArrayOutputStream())) {
                    byte[] invoiceXml = Files.readAllBytes(entry);

                    InvoiceGenerationParams invoiceGenerationParams = InvoiceGenerationParams.builder()
                            .ksefNumber(ksefNumber)
                            .verificationLink(verificationLink)
                            .logo(logo)
                            .showCorrectionDifferences(showCorrectionDifferences)
                            .schema(schema)
                            .build();

                    generator.generateInvoice(invoiceXml, invoiceGenerationParams, out);
                }
            }
        }
    }
}
