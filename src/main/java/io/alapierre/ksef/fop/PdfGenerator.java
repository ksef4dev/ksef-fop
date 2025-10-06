package io.alapierre.ksef.fop;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.fop.apps.*;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.11
 */
@Slf4j
public class PdfGenerator {

    private final FopFactory fopFactory;
    private InvoicePdfConfig invoicePdfConfig = new InvoicePdfConfig();

    /**
     * Create generator with Apache FOP config file from classpath
     *
     * @param fopConfig config file name
     * @throws IOException            throws when cant load given config file from classpath
     * @throws ConfigurationException throws when config file has errors
     */
    public PdfGenerator(String fopConfig, InvoicePdfConfig invoicePdfConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig));
        this.invoicePdfConfig = invoicePdfConfig;
    }

    /**
     * Create generator with Apache FOP config file from classpath
     *
     * @param fopConfig config file name
     * @throws IOException            throws when cant load given config file from classpath
     * @throws ConfigurationException throws when config file has errors
     */
    public PdfGenerator(String fopConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig));
    }

    /**
     * Create generator with Apache FOP config
     *
     * @param fopConfig InputStream to read FOP config file
     *
     * @throws ConfigurationException  throws when config file has errors
     */
    public PdfGenerator(InputStream fopConfig) throws ConfigurationException {
        val builder = new FopFactoryBuilder(new File(".").toURI());
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.build(fopConfig);
        builder.setConfiguration(cfg);
        fopFactory = builder.build();
    }

    /**
     * Generates UPO PDF from given XML and OutputStream (defaults to v3 for backward compatibility)
     * @param upoXML UPO XML
     * @param out    destination OutputStream
     * @throws IOException          throws when IO error occurs
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException         throws when FOP error occurs
     * @deprecated Use generateUpo(Source, UpoGenerationParams, OutputStream) instead for version control
     */
    @Deprecated
    public void generateUpo(Source upoXML, OutputStream out) throws IOException, TransformerException, FOPException {
        UpoGenerationParams params = UpoGenerationParams.builder()
                .schema(UpoSchema.UPO_V3)
                .build();
        generateUpo(upoXML, params, out);
    }

    /**
     * Generates UPO PDF from given XML and OutputStream with version support
     * @param upoXML UPO XML
     * @param params UPO generation parameters including schema version
     * @param out    destination OutputStream
     * @throws IOException          throws when IO error occurs
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException         throws when FOP error occurs
     */
    public void generateUpo(Source upoXML, UpoGenerationParams params, OutputStream out) throws IOException, TransformerException, FOPException {

        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);
        TransformerFactory factory = TransformerFactory.newInstance();

        String templatePath = getUpoTemplatePathForSchema(params);
        Transformer transformer = factory.newTransformer(new StreamSource(loadResource(templatePath)));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(upoXML, res);
    }

    /**
     * Generates a regular invoice PDF based on the input XML, using InvoiceGenerationParams for configurable options.
     *
     * This method is responsible for creating a new invoice and does not handle duplicate-related logic.
     *
     * @param invoiceXml e-invoice FA(2) XML
     * @param params An instance of InvoiceGenerationParams, containing settings like KSeF number, verification link,
     *               QR code, and logo.
     * @param out The OutputStream where the generated PDF will be written.
     * @throws IOException throws when IO error occurs
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException throws when FOP error occurs
     */
    public void generateInvoice(Source invoiceXml, InvoiceGenerationParams params, OutputStream out)
            throws IOException, TransformerException, FOPException {
        generatePdfInvoice(invoiceXml, params, null, out);
    }

    /**
     * Generates a duplicate invoice PDF based on the input XML, using InvoiceGenerationParams for configurable options.
     *
     * This method specifically handles the generation of a duplicate invoice, inserting the duplicate date.
     *
     * @param invoiceXml The source XML file representing the e-invoice FA(2) format.
     * @param params An instance of InvoiceGenerationParams, containing settings like KSeF number, verification link,
     *               QR code, logo, and duplicate date.
     * @param duplicateDate The date when the duplicate invoice was issued.
     * @param out The OutputStream where the generated PDF will be written.
     * @throws IOException If an I/O error occurs during the generation process.
     * @throws TransformerException If an error occurs during the XSLT transformation.
     * @throws FOPException If an error occurs during the PDF rendering process.
     */
    public void generateDuplicateInvoice(Source invoiceXml, InvoiceGenerationParams params, LocalDate duplicateDate, OutputStream out)
            throws IOException, TransformerException, FOPException {
        generatePdfInvoice(invoiceXml, params, duplicateDate, out);
    }

    /**
     * Internal method that performs the actual generation of the PDF, applying any additional data such as
     * KSeF number, QR codes, and handling duplicate invoices if applicable.
     *
     * @param invoiceXml The source XML representing the invoice.
     * @param params An instance of InvoiceGenerationParams containing additional data for the invoice.
     * @param duplicateDate The date the duplicate invoice was issued (can be null for regular invoices).
     * @param out The OutputStream where the generated PDF will be written.
     * @throws FOPException If an error occurs during the FOP processing.
     * @throws IOException If an I/O error occurs.
     * @throws TransformerException If an error occurs during the XSLT transformation.
     */
    private void generatePdfInvoice(@NotNull Source invoiceXml,
                                    InvoiceGenerationParams params,
                                    @Nullable LocalDate duplicateDate,
                                    OutputStream out)
            throws FOPException, IOException, TransformerException {
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);

        TransformerFactory factory = new TransformerFactoryImpl();

        String xslFileName = getXslTemplatePathForSchema(params);

        InputStream xslInputStream = loadResource(xslFileName);
        URL xslUrl = getClass().getClassLoader().getResource(xslFileName);

        StreamSource xslSource = new StreamSource(xslInputStream, xslUrl.toExternalForm());

        Transformer transformer = factory.newTransformer(xslSource);

        insertAdditionalInvoiceData(params, duplicateDate, transformer);

        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(invoiceXml, res);
    }

    private static @NotNull String getXslTemplatePathForSchema(InvoiceGenerationParams params) {
        String xslFileName;
        switch (params.getSchema()) {
            case FA2_1_0_E -> xslFileName = "templates/fa2/ksef_invoice.xsl";
            case FA3_1_0_E -> xslFileName = "templates/fa3/ksef_invoice.xsl";
            default -> {
                log.warn("Schema is not provided in InvoiceGenerationParams or not supported, using default FA2");
                xslFileName = "templates/fa2/ksef_invoice.xsl";
            }
        }
        return xslFileName;
    }

    private static @NotNull String getUpoTemplatePathForSchema(UpoGenerationParams params) {
        String templateFileName;
        switch (params.getSchema()) {
            case UPO_V3 -> templateFileName = "templates/upo_v3/ksef_upo.fop";
            case UPO_V4_2 -> templateFileName = "templates/upo_v4/ksef_upo.fop";
            default -> {
                log.warn("UPO Schema is not provided in UpoGenerationParams or not supported, using default v3");
                templateFileName = "templates/upo_v3/ksef_upo.fop";
            }
        }
        return templateFileName;
    }

    private void insertAdditionalInvoiceData(InvoiceGenerationParams params, @Nullable LocalDate duplicateDate, @NotNull Transformer transformer) {
        Optional.ofNullable(params.getQrCode())
                .map(Base64.getEncoder()::encodeToString)
                .ifPresent(encodedQrCode -> setParameterIfNotNull("qrCode", encodedQrCode, transformer));
        Optional.ofNullable(params.getLogo())
                .map(Base64.getEncoder()::encodeToString)
                .ifPresent(encodedLogo -> setParameterIfNotNull("logo", encodedLogo, transformer));
        Optional.ofNullable(duplicateDate)
                .map(localDate -> localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .ifPresent(formattedDate -> setParameterIfNotNull("duplicateDate", formattedDate, transformer));
        Optional.ofNullable(params.getCurrencyDate())
                .map(localDate -> localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .ifPresent(formattedDate -> setParameterIfNotNull("currencyDate", formattedDate, transformer));
        setParameterIfNotNull("nrKsef", params.getKsefNumber(), transformer);
        setParameterIfNotNull("verificationLink", params.getVerificationLink(), transformer);
        setParameterIfNotNull("showFooter", invoicePdfConfig.isShowFooter(), transformer);
        setParameterIfNotNull("useExtendedDecimalPlaces", invoicePdfConfig.isUseExtendedPriceDecimalPlaces(), transformer);
        setParameterIfNotNull("issuerUser", params.getIssuerUser(), transformer);
        setParameterIfNotNull("showCorrectionDifferences", params.isShowCorrectionDifferences(), transformer);
    }

    private void setParameterIfNotNull(@NotNull String name,
                                       @Nullable Object value,
                                       @NotNull Transformer transformer) {
        if (value != null) {
            transformer.setParameter(name, value);
        }
    }

    private static InputStream loadResource(String resource) throws IOException {
        val res = PdfGenerator.class.getClassLoader().getResourceAsStream(resource);
        if (res == null) throw new IOException("Can't load classpath resource " + resource);
        return res;
    }

}
