package io.alapierre.ksef.fop;

import io.alapierre.ksef.fop.i18n.TranslationService;
import io.alapierre.ksef.fop.qr.QrCodeBuilder;
import io.alapierre.ksef.fop.qr.QrCodeData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.11
 */
@Slf4j
public class PdfGenerator {

    private static final String MIME_PDF = "application/pdf";

    private final FopFactory fopFactory;
    private InvoicePdfConfig invoicePdfConfig = new InvoicePdfConfig();
    private final TranslationService translationService = new TranslationService();
    private final QrCodeBuilder qrCodeBuilder = new QrCodeBuilder(translationService);

    public PdfGenerator(String fopConfig, InvoicePdfConfig invoicePdfConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig));
        this.invoicePdfConfig = invoicePdfConfig;
    }

    public PdfGenerator(String fopConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig));
    }

    public PdfGenerator(InputStream fopConfig, InvoicePdfConfig invoicePdfConfig) throws ConfigurationException {
        this(fopConfig);
        this.invoicePdfConfig = invoicePdfConfig;
    }

    public PdfGenerator(InputStream fopConfig) throws ConfigurationException {
        URI baseUri = new File(".").toURI();
        ClasspathResourceResolver resourceResolver = new ClasspathResourceResolver();
        InternalResourceResolver internalResourceResolver = ResourceResolverFactory.createInternalResourceResolver(baseUri, resourceResolver);
        FopFactoryBuilder builder = new FopFactoryBuilder(baseUri, resourceResolver);
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.build(fopConfig);
        builder.setConfiguration(cfg);
        builder.getFontManager().setResourceResolver(internalResourceResolver);
        this.fopFactory = builder.build();
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

        try {
            Document labels = translationService.getTranslationsAsXml(params.getLanguage().getCode());
            transformer.setParameter("labels", labels);
        } catch (Exception e) {
            log.error("Failed to load translations", e);
        }

        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(upoXML, res);
    }

    /**
     * Generates a regular invoice PDF based on the input XML, using InvoiceGenerationParams for configurable options.
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
    public void generateInvoice(byte[] invoiceXml,
                                InvoiceGenerationParams params,
                                OutputStream out) throws IOException, TransformerException, FOPException {
        String langCode = params.getLanguage().getCode();
        List<QrCodeData> qrCodes = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), invoiceXml, langCode);
        generatePdfInvoice(invoiceXml, params, qrCodes, null, out);
    }

    /**
     * Generates a duplicate invoice PDF based on the input XML, using InvoiceGenerationParams for configurable options.
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
    @SuppressWarnings("unused")
    public void generateDuplicateInvoice(byte[] invoiceXml,
                                         InvoiceGenerationParams params,
                                         LocalDate duplicateDate,
                                         OutputStream out) throws IOException, TransformerException, FOPException {
        generatePdfInvoice(invoiceXml, params, null, duplicateDate, out);
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
    private void generatePdfInvoice(byte @NotNull [] invoiceXml,
                                    InvoiceGenerationParams params,
                                    @Nullable List<QrCodeData> qrCodes,
                                    @Nullable LocalDate duplicateDate,
                                    OutputStream out)
            throws FOPException, IOException, TransformerException {

        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(MIME_PDF, foUserAgent, out);

        TransformerFactory factory = new TransformerFactoryImpl();
        factory.setURIResolver(new ClasspathUriResolver());
        String xslPath = resolveXslTemplate(params);

        URL xslUrl = getResourceUrl(xslPath);
        try (InputStream xsl = loadResource(xslPath)) {
            Transformer transformer = factory.newTransformer(new StreamSource(xsl, xslUrl.toExternalForm()));
            applyParameters(params, qrCodes, duplicateDate, transformer);

            Source xmlSource = new StreamSource(new ByteArrayInputStream(invoiceXml));
            Result result = new SAXResult(fop.getDefaultHandler());
            transformer.transform(xmlSource, result);
        }
    }

    private static @NotNull String getUpoTemplatePathForSchema(UpoGenerationParams params) {
        String templateFileName;
        switch (params.getSchema()) {
            case UPO_V3:
                templateFileName = "templates/upo_v3/ksef_upo.fop";
                break;
            case UPO_V4_2:
                templateFileName = "templates/upo_v4/ksef_upo_v4_2.fop";
                break;
            case UPO_V4_3:
                templateFileName = "templates/upo_v4/ksef_upo_v4_3.fop";
                break;
            default:
                log.warn("UPO Schema is not provided in UpoGenerationParams or not supported, using default v3");
                templateFileName = "templates/upo_v3/ksef_upo.fop";
        }
        return templateFileName;
    }

    private void applyParameters(InvoiceGenerationParams params,
                                 @Nullable List<QrCodeData> qrCodes,
                                 @Nullable LocalDate duplicateDate,
                                 @NotNull Transformer transformer) {

        try {
            Document labels = translationService.getTranslationsAsXml(params.getLanguage().getCode());
            setParam(transformer, "labels", labels);
        } catch (Exception e) {
            log.error("Failed to load translations", e);
        }

        setQrParameters(qrCodes, transformer);

        if (params.getLogo() != null) {
            setParam(transformer, "logo", Base64.getEncoder().encodeToString(params.getLogo()));
        }

        if (params.getLogoUri() != null) {
            setParam(transformer, "logoUri", params.getLogoUri().toString());
        }

        if (duplicateDate != null) {
            setParam(transformer, "duplicateDate", duplicateDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (params.getCurrencyDate() != null) {
            setParam(transformer, "currencyDate", params.getCurrencyDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        // proste flagi/teksty
        setParam(transformer, "nrKsef", params.getKsefNumber());
        setParam(transformer, "showFooter", invoicePdfConfig.isShowFooter());
        setParam(transformer, "useExtendedDecimalPlaces", invoicePdfConfig.isUseExtendedPriceDecimalPlaces());
        setParam(transformer, "issuerUser", params.getIssuerUser());
        setParam(transformer, "showCorrectionDifferences", params.isShowCorrectionDifferences());
    }

    private void setQrParameters(@Nullable List<QrCodeData> qrCodes, @NotNull Transformer transformer) {
        int count = (qrCodes == null) ? 0 : qrCodes.size();
        setParam(transformer, "qrCodesCount", count);

        if (qrCodes == null) return;

        for (int i = 0; i < qrCodes.size(); i++) {
            QrCodeData qr = qrCodes.get(i);
            String idx = String.valueOf(i);

            setParam(transformer, "qrCode" + idx, Base64.getEncoder().encodeToString(qr.getQrCodeImage()));
            setParam(transformer, "qrCodeLabel" + idx, qr.getLabel());
            setParam(transformer, "verificationLink" + idx, qr.getVerificationLink());
            setParam(transformer, "verificationLinkTitle" + idx, qr.getVerificationLinkTitle());
        }
    }

    private void setParam(@NotNull Transformer transformer, @NotNull String name, @Nullable Object value) {
        if (value != null) transformer.setParameter(name, value);
    }



    private static InputStream loadResource(String resource) throws IOException {
        val res = PdfGenerator.class.getClassLoader().getResourceAsStream(resource);
        if (res == null) throw new IOException("Can't load classpath resource " + resource);
        return res;
    }

    private URL getResourceUrl(String resource) throws IOException {
        URL url = getClass().getClassLoader().getResource(resource);
        if (url == null) throw new IOException("Can't resolve classpath resource URL " + resource);
        return url;
    }

    private static String resolveXslTemplate(InvoiceGenerationParams params) {
        switch (params.getSchema()) {
            case FA2_1_0_E:
                return "templates/fa2/ksef_invoice.xsl";
            case FA3_1_0_E:
                return "templates/fa3/ksef_invoice.xsl";
            default:
                log.warn("UPO Schema {} in InvoiceGenerationParams is not supported, using default {}", params.getSchema(), InvoiceSchema.FA3_1_0_E);
                return "templates/fa3/ksef_invoice.xsl";
        }
    }

}
