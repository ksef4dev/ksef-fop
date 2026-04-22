package io.alapierre.ksef.fop;

import io.alapierre.ksef.fop.i18n.TranslationService;
import io.alapierre.ksef.fop.internal.Strings;
import io.alapierre.ksef.fop.internal.TemplateResolver;
import io.alapierre.ksef.fop.qr.QrCodeBuilder;
import io.alapierre.ksef.fop.qr.QrCodeData;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.fop.apps.*;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.11
 */
@Slf4j
public class PdfGenerator {

    private static final String MIME_PDF = "application/pdf";

    private final InvoicePdfConfig invoicePdfConfig;
    private final Configuration fopConfiguration;

    public PdfGenerator(String fopConfig, InvoicePdfConfig invoicePdfConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig), invoicePdfConfig);
    }

    public PdfGenerator(String fopConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig), new InvoicePdfConfig());
    }

    public PdfGenerator(InputStream fopConfig) throws ConfigurationException {
        this(fopConfig, new InvoicePdfConfig());
    }

    public PdfGenerator(InputStream fopConfig, InvoicePdfConfig invoicePdfConfig) throws ConfigurationException {
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        this.fopConfiguration = cfgBuilder.build(fopConfig);
        this.invoicePdfConfig = invoicePdfConfig;
    }

    /**
     * Generates UPO PDF from given XML and OutputStream (defaults to v3 for backward compatibility)
     * @param upoXML UPO XML
     * @param out    destination OutputStream
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException         throws when FOP error occurs
     * @deprecated Use generateUpo(Source, UpoGenerationParams, OutputStream) instead for version control
     */
    @Deprecated
    public void generateUpo(Source upoXML, OutputStream out) throws TransformerException, FOPException {
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
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException         throws when FOP error occurs
     */
    public void generateUpo(Source upoXML, UpoGenerationParams params, OutputStream out) throws TransformerException, FOPException {
        FopFactory fopFactory = createFopFactory();
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(MIME_PDF, foUserAgent, out);

        TemplateResolver resolver = new TemplateResolver(params.getTemplateRoots());
        TranslationService translationService = new TranslationService(
                resolver,
                params.getTranslationDocumentBuilderFactoryCustomizer());
        Transformer transformer = createTransformer(resolver, resolveUpoTemplatePath(params));
        applyLabelParameters(translationService, params.resolveLanguageTag(), transformer);

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
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException throws when FOP error occurs
     */
    public void generateInvoice(byte[] invoiceXml,
                                InvoiceGenerationParams params,
                                OutputStream out) throws TransformerException, FOPException {
        String langCode = params.resolveLanguageTag();
        TemplateResolver resolver = new TemplateResolver(params.getTemplateRoots());
        TranslationService translationService = new TranslationService(
                resolver,
                params.getTranslationDocumentBuilderFactoryCustomizer());
        QrCodeBuilder qrCodeBuilder = new QrCodeBuilder(translationService);
        List<QrCodeData> qrCodes = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), invoiceXml, langCode);
        generatePdfInvoice(invoiceXml, params, qrCodes, null, resolver, translationService, out);
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
     * @throws TransformerException If an error occurs during the XSLT transformation.
     * @throws FOPException If an error occurs during the PDF rendering process.
     */
    @SuppressWarnings("unused")
    public void generateDuplicateInvoice(byte[] invoiceXml,
                                         InvoiceGenerationParams params,
                                         LocalDate duplicateDate,
                                         OutputStream out) throws TransformerException, FOPException {
        TemplateResolver resolver = new TemplateResolver(params.getTemplateRoots());
        TranslationService translationService = new TranslationService(
                resolver,
                params.getTranslationDocumentBuilderFactoryCustomizer());
        generatePdfInvoice(invoiceXml, params, null, duplicateDate, resolver, translationService, out);
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
     * @throws TransformerException If an error occurs during the XSLT transformation.
     */
    private void generatePdfInvoice(byte @NotNull [] invoiceXml,
                                    InvoiceGenerationParams params,
                                    @Nullable List<QrCodeData> qrCodes,
                                    @Nullable LocalDate duplicateDate,
                                    TemplateResolver resolver,
                                    TranslationService translationService,
                                    OutputStream out)
            throws FOPException, TransformerException {

        FopFactory fopFactory = createFopFactory();
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(MIME_PDF, foUserAgent, out);

        Transformer transformer = createTransformer(resolver, resolveTemplatePath(params));

        applyParameters(params, qrCodes, duplicateDate, translationService, transformer);

        Source xmlSource = new StreamSource(new ByteArrayInputStream(invoiceXml));
        Result result = new SAXResult(fop.getDefaultHandler());
        transformer.transform(xmlSource, result);
    }

    private static Transformer createTransformer(TemplateResolver resolver, String systemId) throws TransformerException {
        return createTransformerFactory(resolver).newTransformer(resolver.resolve(systemId, null));
    }

    private static TransformerFactory createTransformerFactory(TemplateResolver resolver) throws TransformerException {
        TransformerFactory factory = new TransformerFactoryImpl();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setURIResolver(resolver);
        return factory;
    }

    private @NotNull String resolveTemplatePath(InvoiceGenerationParams params) {
        String templatePath = params.getTemplatePath();
        return Strings.isEmpty(templatePath)
                ? resolveXslTemplate(params)
                : templatePath;
    }

    private static @NotNull String resolveUpoTemplatePath(UpoGenerationParams params) {
        String templatePath = params.getTemplatePath();
        return Strings.isEmpty(templatePath)
                ? getUpoTemplatePathForSchema(params)
                : templatePath;
    }

    private static @NotNull String getUpoTemplatePathForSchema(UpoGenerationParams params) {
        switch (params.getSchema()) {
            case UPO_V3:
                return "templates/upo_v3/ksef_upo.fop";
            case UPO_V4_2:
                return "templates/upo_v4/ksef_upo_v4_2.fop";
            case UPO_V4_3:
                return "templates/upo_v4/ksef_upo_v4_3.fop";
            default:
                log.warn("UPO Schema is not provided in UpoGenerationParams or not supported, using default v3");
                return "templates/upo_v3/ksef_upo.fop";
        }
    }

    private void applyParameters(InvoiceGenerationParams params,
                                 @Nullable List<QrCodeData> qrCodes,
                                 @Nullable LocalDate duplicateDate,
                                 @NotNull TranslationService translationService,
                                 @NotNull Transformer transformer) {

        applyLabelParameters(translationService, params.resolveLanguageTag(), transformer);

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

        setParam(transformer, "nrKsef", params.getKsefNumber());
        setParam(transformer, "showFooter", invoicePdfConfig.isShowFooter());
        setParam(transformer, "useExtendedDecimalPlaces", invoicePdfConfig.isUseExtendedPriceDecimalPlaces());
        setParam(transformer, "issuerUser", params.getIssuerUser());
        setParam(transformer, "showCorrectionDifferences", params.isShowCorrectionDifferences());

        Map<String, Object> customProperties = params.getCustomProperties();
        if (customProperties != null) {
            customProperties.forEach((key, value) -> setParam(transformer, key, value));
        }
    }

    /**
     * Injects the fully-merged label document as the XSLT {@code labels} parameter.
     *
     * <p>The merge is performed Java-side ({@link TranslationService#getTranslationsAsXml})
     * because it needs to overlay partial filesystem overrides on top of the classpath
     * defaults — a URIResolver called from XSLT via {@code document()} only returns the
     * first hit and would shadow missing keys with empty text. The XSLT uses
     * {@code <xsl:key name="kLabels" match="entry" use="@key"/>} against this parameter to
     * look up individual entries.</p>
     */
    private static void applyLabelParameters(@NotNull TranslationService translationService,
                                             @NotNull String lang,
                                             @NotNull Transformer transformer) {
        Document labels = translationService.getTranslationsAsXml(lang);
        transformer.setParameter("labels", labels);
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

    private FopFactory createFopFactory() {
        URI baseUri = new File(".").toURI();
        ClasspathResourceResolver resourceResolver = new ClasspathResourceResolver();
        InternalResourceResolver internalResourceResolver = ResourceResolverFactory.createInternalResourceResolver(baseUri, resourceResolver);
        FopFactoryBuilder builder = new FopFactoryBuilder(baseUri, resourceResolver);

        builder.setConfiguration(fopConfiguration);
        builder.getFontManager().setResourceResolver(internalResourceResolver);
        return builder.build();
    }

    private static InputStream loadResource(String resource) throws IOException {
        InputStream res = PdfGenerator.class.getClassLoader().getResourceAsStream(resource);
        if (res == null) throw new IOException("Can't load classpath resource " + resource);
        return res;
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
