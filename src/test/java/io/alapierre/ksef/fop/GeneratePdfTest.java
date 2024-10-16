package io.alapierre.ksef.fop;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
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

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.11
 */
class GeneratePdfTest {

    @Test
    void genByService() throws Exception {

        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC.xml");
            Source src = new StreamSource(xml);
            generator.generateUpo(src, out);
        }
    }

    @Test
    void genWithConfFromClasspath() throws Exception {

        PdfGenerator generator = new PdfGenerator("fop.xconf");

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC.xml");
            Source src = new StreamSource(xml);
            generator.generateUpo(src, out);
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
            Transformer transformer = factory.newTransformer(new StreamSource("src/main/resources/ksef_upo.fop"));

            InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC.xml");
            Source src = new StreamSource(xml);
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        }
    }

    @Test
    void generateInvoicePdf() throws Exception {
        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/invoice.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/faktury/podstawowa/FA_2_Przyklad_20.xml");
            Source src = new StreamSource(xml);
            InvoiceGenerationParams invoiceGenerationParams = new InvoiceGenerationParams(null, null, null, null);
            generator.generateInvoice(src, invoiceGenerationParams, out);
        }
    }

    @Test
    void generateInvoicePdfWithAdditionalData() throws Exception {
        String ksefNumber = "6891152920-20231221-B3242FB4B54B-DF";
        String verificationLink = "https://ksef-test.mf.gov.pl/web/verify/6891152920-20231221-B3242FB4B54B-DF/ssTckvmMFEeA3vp589ExHzTRVhbDksjcFzKoXi4K%2F%2F0%3D";
        File qrCodeFile = new File("src/test/resources/barcode.png");
        byte[] qrCode = Files.readAllBytes(qrCodeFile.toPath());
        File logoFile = new File("src/test/resources/Logo.svg");
        byte[] logo = Files.readAllBytes(logoFile.toPath());

        PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/invoice.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/faktury/podstawowa/FA_2_Przyklad_20.xml");
            Source src = new StreamSource(xml);
            InvoiceGenerationParams invoiceGenerationParams = new InvoiceGenerationParams(ksefNumber, verificationLink, qrCode, logo);

            generator.generateInvoice(src, invoiceGenerationParams, out);
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
        Path invoiceFolder = Paths.get("src/test/resources/faktury/podstawowa");

        // Pobieranie wszystkich plików XML z folderu
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(invoiceFolder, "*.xml")) {
            for (Path entry : stream) {
                // Tworzenie strumienia dla każdego pliku XML
                try (InputStream xml = Files.newInputStream(entry);
                     OutputStream out = new BufferedOutputStream(new ByteArrayOutputStream())) {

                    Source src = new StreamSource(xml);
                    InvoiceGenerationParams invoiceGenerationParams = new InvoiceGenerationParams(ksefNumber, verificationLink, qrCode, logo);

                    generator.generateInvoice(src, invoiceGenerationParams, out);
                }
            }
        }
    }
}
