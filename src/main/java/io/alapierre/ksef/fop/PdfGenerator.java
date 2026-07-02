package io.alapierre.ksef.fop;

import io.alapierre.ksef.fop.i18n.TranslationService;
import io.alapierre.ksef.fop.internal.Strings;
import io.alapierre.ksef.fop.internal.TemplateResolver;
import io.alapierre.ksef.fop.internal.XmlFactories;
import io.alapierre.ksef.fop.qr.QrCodeBuilder;
import io.alapierre.ksef.fop.qr.QrCodeData;
import lombok.extern.slf4j.Slf4j;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.configuration.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

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

    private final InvoicePdfConfig invoicePdfConfig;
    private final FopRendererPool fopRenderer;

    /**
     * Creates a generator from a repeatable classpath FOP configuration resource and invoice PDF options.
     *
     * <p>This constructor supports {@link InvoicePdfConfig#getRendererPoolSize()} values greater than
     * {@code 1}, because the configuration resource can be opened separately for each renderer.</p>
     *
     * @param fopConfig classpath location of the FOP configuration file
     * @param invoicePdfConfig invoice PDF rendering options
     * @throws IOException if the configuration resource cannot be loaded
     * @throws ConfigurationException if the FOP configuration cannot be parsed
     */
    public PdfGenerator(String fopConfig, InvoicePdfConfig invoicePdfConfig) throws IOException, ConfigurationException {
        this(() -> loadResource(fopConfig), invoicePdfConfig);
    }

    /**
     * Creates a generator from a repeatable classpath FOP configuration resource using default invoice PDF options.
     *
     * @param fopConfig classpath location of the FOP configuration file
     * @throws IOException if the configuration resource cannot be loaded
     * @throws ConfigurationException if the FOP configuration cannot be parsed
     */
    public PdfGenerator(String fopConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig), new InvoicePdfConfig());
    }

    /**
     * Creates a generator from a one-shot FOP configuration stream using default invoice PDF options.
     *
     * <p>A raw {@link InputStream} can only be consumed once, so this constructor effectively supports
     * only a single renderer. Use the classpath resource constructor when a renderer pool larger than
     * {@code 1} is needed.</p>
     *
     * @param fopConfig stream containing the FOP configuration
     * @throws ConfigurationException if the FOP configuration cannot be parsed
     */
    public PdfGenerator(InputStream fopConfig) throws ConfigurationException {
        this(fopConfig, new InvoicePdfConfig());
    }

    /**
     * Creates a generator from a one-shot FOP configuration stream and invoice PDF options.
     *
     * <p>A raw {@link InputStream} can only be consumed once. If
     * {@link InvoicePdfConfig#getRendererPoolSize()} is greater than {@code 1}, construction fails with
     * {@link ConfigurationException}; use the classpath resource constructor for pooled rendering.</p>
     *
     * @param fopConfig stream containing the FOP configuration
     * @param invoicePdfConfig invoice PDF rendering options
     * @throws ConfigurationException if the FOP configuration cannot be parsed or pooled rendering is requested
     *                                for a one-shot stream
     */
    public PdfGenerator(InputStream fopConfig, InvoicePdfConfig invoicePdfConfig) throws ConfigurationException {
        this.fopRenderer = new FopRendererPool(fopConfig, invoicePdfConfig.getRendererPoolSize());
        this.invoicePdfConfig = invoicePdfConfig;
    }

    private PdfGenerator(FopRendererPool.FopConfigSource fopConfigSource, InvoicePdfConfig invoicePdfConfig)
            throws IOException, ConfigurationException {
        this.fopRenderer = new FopRendererPool(fopConfigSource, invoicePdfConfig.getRendererPoolSize());
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
        fopRenderer.render(out, fop -> {
            String upoTemplatePath = resolveUpoTemplatePath(params);
            TemplateResolver resolver = createTemplateResolver(params.getResourceRoots(), upoTemplatePath);
            TranslationService translationService = new TranslationService(resolver, upoTemplatePath);
            Templates template = XmlFactories.getTemplate(resolver, upoTemplatePath);
            Transformer transformer = template.newTransformer();
            applyLabelParameters(translationService, params.resolveLanguageTag(), transformer);

            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(upoXML, res);
        });
    }

    /**
     * Generates a regular invoice PDF based on the input XML, using InvoiceGenerationParams for configurable options.
     * This method is responsible for creating a new invoice and does not handle duplicate-related logic.
     *
     * @param invoiceXml KSeF invoice XML matching the schema selected in {@code params}
     * @param params An instance of InvoiceGenerationParams, containing settings like KSeF number, verification link,
     *               QR code, and logo.
     * @param out The OutputStream where the generated PDF will be written.
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException throws when FOP error occurs
     */
    public void generateInvoice(byte[] invoiceXml,
                                InvoiceGenerationParams params,
                                OutputStream out) throws TransformerException, FOPException {
        fopRenderer.render(out, fop -> {
            String langCode = params.resolveLanguageTag();
            String templatePath = resolveTemplatePath(params);
            TemplateResolver resolver = createTemplateResolver(params.getResourceRoots(), templatePath);
            TranslationService translationService = new TranslationService(resolver, templatePath);
            QrCodeBuilder qrCodeBuilder = new QrCodeBuilder(translationService);
            List<QrCodeData> qrCodes = qrCodeBuilder.buildQrCodes(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), invoiceXml, langCode);
            generatePdfInvoice(invoiceXml, params, qrCodes, null, resolver, translationService, fop);
        });
    }

    /**
     * Generates a duplicate invoice PDF based on the input XML, using InvoiceGenerationParams for configurable options.
     * This method specifically handles the generation of a duplicate invoice, inserting the duplicate date.
     *
     * @param invoiceXml KSeF invoice XML matching the schema selected in {@code params}.
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
        fopRenderer.render(out, fop -> {
            String templatePath = resolveTemplatePath(params);
            TemplateResolver resolver = createTemplateResolver(params.getResourceRoots(), templatePath);
            TranslationService translationService = new TranslationService(resolver, templatePath);
            generatePdfInvoice(invoiceXml, params, null, duplicateDate, resolver, translationService, fop);
        });
    }

    /**
     * Internal method that performs the actual generation of the PDF, applying any additional data such as
     * KSeF number, QR codes, and handling duplicate invoices if applicable.
     *
     * @param invoiceXml The source XML representing the invoice.
     * @param params An instance of InvoiceGenerationParams containing additional data for the invoice.
     * @param qrCodes QR code data to include in the PDF, or null when no QR codes should be rendered.
     * @param duplicateDate The date the duplicate invoice was issued (can be null for regular invoices).
     * @param resolver Resolver used for templates, labels, and resources referenced during transformation.
     * @param translationService Service used to resolve translated labels for the selected language.
     * @param fop FOP instance receiving SAX events and writing the generated PDF.
     * @throws FOPException If an error occurs during the FOP processing.
     * @throws TransformerException If an error occurs during the XSLT transformation.
     */
    private void generatePdfInvoice(byte @NotNull [] invoiceXml,
                                    InvoiceGenerationParams params,
                                    @Nullable List<QrCodeData> qrCodes,
                                    @Nullable LocalDate duplicateDate,
                                    TemplateResolver resolver,
                                    TranslationService translationService,
                                    Fop fop)
            throws FOPException, TransformerException {

        String stylesheetPath = resolveTemplatePath(params);
        Templates template = XmlFactories.getTemplate(resolver, stylesheetPath);
        Transformer transformer = template.newTransformer();

        applyParameters(params, qrCodes, duplicateDate, resolver, translationService, transformer);

        Source xmlSource = new StreamSource(new ByteArrayInputStream(invoiceXml));
        Result result = new SAXResult(fop.getDefaultHandler());
        transformer.transform(xmlSource, result);
    }

    private static TemplateResolver createTemplateResolver(List<URI> resourceRoots,
                                                           String templatePath) throws TransformerException {
        TemplateResolver resolver = new TemplateResolver(resourceRoots);
        requireHttpTemplatePathUnderResourceRoots(resolver, templatePath);
        return resolver;
    }

    /**
     * When HTTP resource roots are configured, an HTTP(S) {@code templatePath} must sit under
     * one of them so misconfiguration fails at setup time. With no HTTP roots, an HTTP
     * {@code templatePath} may still resolve via the XML catalog to a classpath resource.
     */
    private static void requireHttpTemplatePathUnderResourceRoots(@NotNull TemplateResolver resolver,
                                                                  @NotNull String templatePath)
            throws TransformerException {
        if (!resolver.hasHttpResourceRoots() || Strings.isEmpty(templatePath)) {
            return;
        }
        String trimmed = templatePath.trim();
        if (!trimmed.startsWith(TemplateResolver.HTTP_PREFIX)
                && !trimmed.startsWith(TemplateResolver.HTTPS_PREFIX)) {
            return;
        }
        String normalized = URI.create(trimmed).normalize().toString();
        if (!resolver.isUnderAnyHttpRoot(normalized)) {
            throw new TransformerException(
                    "HTTP templatePath is not under any configured HTTP resource root: templatePath="
                            + templatePath);
        }
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
                                 @NotNull TemplateResolver resolver,
                                 @NotNull TranslationService translationService,
                                 @NotNull Transformer transformer) throws TransformerException {

        applyLabelParameters(translationService, params.resolveLanguageTag(), transformer);

        setQrParameters(qrCodes, transformer);

        if (params.getLogo() != null) {
            setParam(transformer, "logo", Base64.getEncoder().encodeToString(params.getLogo()));
        }

        if (params.getLogoUri() != null) {
            setParam(transformer, "logoUri", resolveLogoUri(resolver, params.getLogoUri()));
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

    /**
     * Resolves a logo URI against configured resource roots when it is a relative path;
     * absolute {@code file:} / {@code http(s):} URIs are passed through unchanged.
     */
    private static String resolveLogoUri(@NotNull TemplateResolver resolver, @NotNull URI logoUri)
            throws TransformerException {
        String scheme = logoUri.getScheme();
        if (scheme != null) {
            String lower = scheme.toLowerCase(java.util.Locale.ROOT);
            if ("file".equals(lower) || "http".equals(lower) || "https".equals(lower)) {
                return logoUri.toString();
            }
        }
        String path = logoUri.getPath();
        if (path == null || path.isEmpty()) {
            return logoUri.toString();
        }
        return resolver.tryResolvePublicUri(path)
                .orElse(logoUri.toString());
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
            case FA_RR_1_1_E:
                return "templates/fa_rr/ksef_invoice.xsl";
            default:
                log.warn("UPO Schema {} in InvoiceGenerationParams is not supported, using default {}", params.getSchema(), InvoiceSchema.FA3_1_0_E);
                return "templates/fa3/ksef_invoice.xsl";
        }
    }

    /**
     * Clears the in-memory cache of compiled XSLT templates used by PDF generation.
     * Safe to call from any thread; the next render recompiles from the current template source.
     */
    public static void clearCompiledTemplateCache() {
        XmlFactories.clearCompiledTemplateCache();
    }

}
