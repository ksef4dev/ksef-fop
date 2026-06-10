package io.alapierre.ksef.fop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InvoiceGenerationParams {
    @Nullable
    private String verificationLink;

    private byte[] logo;

    private URI logoUri;

    @Nullable
    private LocalDate currencyDate;

    @Nullable
    private String issuerUser;

    private boolean showCorrectionDifferences;

    @NotNull
    private InvoiceSchema schema;

    /**
     * KSeF Number if provided, OFFLINE label will shown otherwise
     */
    @Nullable
    private String ksefNumber;

    @Nullable
    private InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest;

    /**
     * Optional classpath-relative path to a custom XSLT invoice template.
     * <p>
     * Security note: This value must reference a trusted stylesheet available on the application's classpath.
     * The library does not validate where this path comes from; callers are responsible for ensuring that
     * untrusted users cannot control this value or the underlying XSLT content.
     */
    @Nullable
    private String templatePath;

    /**
     * Optional template-specific XSLT parameters forwarded to the transformer.
     * <p>
     * Security note: Values in this map are passed directly as XSLT parameters.
     * The library does not validate parameter names or values; callers are responsible for ensuring that
     * untrusted users cannot control this map when rendering trusted templates.
     */
    private Map<String, Object> customProperties;

    /**
     * @deprecated use {@link #languageLocale} instead, which accepts any BCP&nbsp;47
     * language tag (e.g. {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}) and is not
     * limited to the values defined by this enum. Kept for backward compatibility.
     * When both are set, {@link #languageLocale} wins (see {@link #resolveLanguageTag()}).
     */
    @Deprecated
    private Language language = Language.PL;

    /**
     * Optional BCP&nbsp;47 language tag used to select the label file for translations
     * (e.g. {@code "en"}, {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}). Both
     * {@code _} and {@code -} separators are accepted. Unknown tags fall back to the
     * default language ({@link Language#DEFAULT_LANGUAGE_TAG}) without raising an error.
     *
     * <p>When set, this value takes precedence over the deprecated {@link #language} enum.</p>
     */
    @Nullable
    private String languageLocale;

    /**
     * Ordered list of filesystem directories searched before the classpath when resolving templates.
     */
    private List<Path> templateRoots;

    /**
     * @deprecated use the builder instead.
     */
    @Deprecated
    public InvoiceGenerationParams() {
        this(builder().schema(InvoiceSchema.FA3_1_0_E));
    }

    /**
     * @deprecated use the builder instead.
     */
    @Deprecated
    public InvoiceGenerationParams(
            @Nullable String verificationLink,
            byte[] logo,
            URI logoUri,
            @Nullable LocalDate currencyDate,
            @Nullable String issuerUser,
            boolean showCorrectionDifferences,
            @NotNull InvoiceSchema schema,
            @Nullable String ksefNumber,
            @Nullable InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest,
            @Nullable String templatePath,
            Map<String, Object> customProperties,
            Language language) {
        this(builder()
                .verificationLink(verificationLink)
                .logo(logo)
                .logoUri(logoUri)
                .currencyDate(currencyDate)
                .issuerUser(issuerUser)
                .showCorrectionDifferences(showCorrectionDifferences)
                .schema(schema)
                .ksefNumber(ksefNumber)
                .invoiceQRCodeGeneratorRequest(invoiceQRCodeGeneratorRequest)
                .templatePath(templatePath)
                .customProperties(customProperties)
                .language(language));
    }

    private InvoiceGenerationParams(InvoiceGenerationParamsBuilder builder) {
        this.verificationLink = builder.verificationLink;
        this.logo = builder.logo;
        this.logoUri = builder.logoUri;
        this.currencyDate = builder.currencyDate;
        this.issuerUser = builder.issuerUser;
        this.showCorrectionDifferences = builder.showCorrectionDifferences;
        this.schema = Objects.requireNonNull(builder.schema, "schema");
        this.ksefNumber = builder.ksefNumber;
        this.invoiceQRCodeGeneratorRequest = builder.invoiceQRCodeGeneratorRequest;
        this.templatePath = builder.templatePath;
        this.customProperties = builder.customProperties;
        this.language = builder.language == null ? Language.PL : builder.language;
        this.languageLocale = builder.languageLocale;
        this.templateRoots = builder.templateRoots == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.templateRoots));
    }

    @Nullable
    public String getVerificationLink() {
        return verificationLink;
    }

    public void setVerificationLink(@Nullable String verificationLink) {
        this.verificationLink = verificationLink;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public URI getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(URI logoUri) {
        this.logoUri = logoUri;
    }

    @Nullable
    public LocalDate getCurrencyDate() {
        return currencyDate;
    }

    public void setCurrencyDate(@Nullable LocalDate currencyDate) {
        this.currencyDate = currencyDate;
    }

    @Nullable
    public String getIssuerUser() {
        return issuerUser;
    }

    public void setIssuerUser(@Nullable String issuerUser) {
        this.issuerUser = issuerUser;
    }

    public boolean isShowCorrectionDifferences() {
        return showCorrectionDifferences;
    }

    public void setShowCorrectionDifferences(boolean showCorrectionDifferences) {
        this.showCorrectionDifferences = showCorrectionDifferences;
    }

    @NotNull
    public InvoiceSchema getSchema() {
        return schema;
    }

    public void setSchema(@NotNull InvoiceSchema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    /**
     * KSeF Number if provided, OFFLINE label will shown otherwise
     */
    @Nullable
    public String getKsefNumber() {
        return ksefNumber;
    }

    public void setKsefNumber(@Nullable String ksefNumber) {
        this.ksefNumber = ksefNumber;
    }

    @Nullable
    public InvoiceQRCodeGeneratorRequest getInvoiceQRCodeGeneratorRequest() {
        return invoiceQRCodeGeneratorRequest;
    }

    public void setInvoiceQRCodeGeneratorRequest(@Nullable InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest) {
        this.invoiceQRCodeGeneratorRequest = invoiceQRCodeGeneratorRequest;
    }

    /**
     * Optional classpath-relative path to a custom XSLT invoice template.
     * <p>
     * Security note: This value must reference a trusted stylesheet available on the application's classpath.
     * The library does not validate where this path comes from; callers are responsible for ensuring that
     * untrusted users cannot control this value or the underlying XSLT content.
     */
    @Nullable
    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(@Nullable String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * Optional template-specific XSLT parameters forwarded to the transformer.
     * <p>
     * Security note: Values in this map are passed directly as XSLT parameters.
     * The library does not validate parameter names or values; callers are responsible for ensuring that
     * untrusted users cannot control this map when rendering trusted templates.
     */
    @Nullable
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, Object> customProperties) {
        this.customProperties = customProperties;
    }

    /**
     * @deprecated use {@link #languageLocale} instead, which accepts any BCP&nbsp;47
     * language tag (e.g. {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}) and is not
     * limited to the values defined by this enum. Kept for backward compatibility.
     * When both are set, {@link #languageLocale} wins (see {@link #resolveLanguageTag()}).
     */
    @Deprecated
    public Language getLanguage() {
        return language;
    }

    /**
     * @deprecated use {@link #languageLocale} instead.
     */
    @Deprecated
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Optional BCP&nbsp;47 language tag used to select the label file for translations
     * (e.g. {@code "en"}, {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}). Both
     * {@code _} and {@code -} separators are accepted. Unknown tags fall back to the
     * default language ({@link Language#DEFAULT_LANGUAGE_TAG}) without raising an error.
     *
     * <p>When set, this value takes precedence over the deprecated {@link #language} enum.</p>
     */
    @Nullable
    public String getLanguageLocale() {
        return languageLocale;
    }

    public void setLanguageLocale(@Nullable String languageLocale) {
        this.languageLocale = languageLocale;
    }

    /**
     * Returns an unmodifiable view of the configured filesystem template roots.
     */
    public List<Path> getTemplateRoots() {
        return templateRoots;
    }

    /**
     * Resolves the effective language tag used for label lookups, in order of precedence:
     * <ol>
     *   <li>{@link #languageLocale} (BCP&nbsp;47 tag, trimmed; blank values are ignored),</li>
     *   <li>{@link #language} ({@code Language} enum, using  Language.getCode()),</li>
     *   <li>{@link Language#DEFAULT_LANGUAGE_TAG}.</li>
     * </ol>
     *
     * <p>This is the single source of truth consumed by the rendering pipeline;
     * callers should not inspect {@link #languageLocale} or {@link #language} directly.</p>
     */
    @NotNull
    public String resolveLanguageTag() {
        if (languageLocale != null) {
            String trimmed = languageLocale.trim();
            if (!trimmed.isEmpty()) return trimmed;
        }
        if (language != null) return language.getCode();
        return Language.DEFAULT_LANGUAGE_TAG;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof InvoiceGenerationParams) {
            InvoiceGenerationParams other = (InvoiceGenerationParams) o;
            return other.canEqual(this)
                    && showCorrectionDifferences == other.showCorrectionDifferences
                    && Arrays.equals(logo, other.logo)
                    && Objects.equals(verificationLink, other.verificationLink)
                    && Objects.equals(logoUri, other.logoUri)
                    && Objects.equals(currencyDate, other.currencyDate)
                    && Objects.equals(issuerUser, other.issuerUser)
                    && Objects.equals(schema, other.schema)
                    && Objects.equals(ksefNumber, other.ksefNumber)
                    && Objects.equals(invoiceQRCodeGeneratorRequest, other.invoiceQRCodeGeneratorRequest)
                    && Objects.equals(templatePath, other.templatePath)
                    && Objects.equals(customProperties, other.customProperties)
                    && Objects.equals(language, other.language)
                    && Objects.equals(languageLocale, other.languageLocale)
                    && Objects.equals(getTemplateRoots(), other.getTemplateRoots());
        }
        return false;
    }

    protected boolean canEqual(Object other) {
        return other instanceof InvoiceGenerationParams;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(verificationLink, logoUri, currencyDate, issuerUser,
                showCorrectionDifferences, schema, ksefNumber, invoiceQRCodeGeneratorRequest,
                templatePath, customProperties, language, languageLocale, getTemplateRoots());
        result = 31 * result + Arrays.hashCode(logo);
        return result;
    }

    @Override
    public String toString() {
        return "InvoiceGenerationParams(verificationLink=" + verificationLink
                + ", logo=" + Arrays.toString(logo)
                + ", logoUri=" + logoUri
                + ", currencyDate=" + currencyDate
                + ", issuerUser=" + issuerUser
                + ", showCorrectionDifferences=" + showCorrectionDifferences
                + ", schema=" + schema
                + ", ksefNumber=" + ksefNumber
                + ", invoiceQRCodeGeneratorRequest=" + invoiceQRCodeGeneratorRequest
                + ", templatePath=" + templatePath
                + ", customProperties=" + customProperties
                + ", language=" + language
                + ", languageLocale=" + languageLocale
                + ", templateRoots=" + getTemplateRoots() + ")";
    }

    public static InvoiceGenerationParamsBuilder builder() {
        return new InvoiceGenerationParamsBuilder();
    }

    public static final class InvoiceGenerationParamsBuilder {

        private String verificationLink;
        private byte[] logo;
        private URI logoUri;
        private LocalDate currencyDate;
        private String issuerUser;
        private boolean showCorrectionDifferences;
        private InvoiceSchema schema;
        private String ksefNumber;
        private InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest;
        private String templatePath;
        private Map<String, Object> customProperties;
        private Language language = Language.PL;
        private String languageLocale;
        private ArrayList<Path> templateRoots;

        InvoiceGenerationParamsBuilder() {
        }

        public InvoiceGenerationParamsBuilder verificationLink(@Nullable String verificationLink) {
            this.verificationLink = verificationLink;
            return this;
        }

        public InvoiceGenerationParamsBuilder logo(byte[] logo) {
            this.logo = logo;
            return this;
        }

        public InvoiceGenerationParamsBuilder logoUri(URI logoUri) {
            this.logoUri = logoUri;
            return this;
        }

        public InvoiceGenerationParamsBuilder currencyDate(@Nullable LocalDate currencyDate) {
            this.currencyDate = currencyDate;
            return this;
        }

        public InvoiceGenerationParamsBuilder issuerUser(@Nullable String issuerUser) {
            this.issuerUser = issuerUser;
            return this;
        }

        public InvoiceGenerationParamsBuilder showCorrectionDifferences(boolean showCorrectionDifferences) {
            this.showCorrectionDifferences = showCorrectionDifferences;
            return this;
        }

        public InvoiceGenerationParamsBuilder schema(@NotNull InvoiceSchema schema) {
            this.schema = Objects.requireNonNull(schema, "schema");
            return this;
        }

        public InvoiceGenerationParamsBuilder ksefNumber(@Nullable String ksefNumber) {
            this.ksefNumber = ksefNumber;
            return this;
        }

        public InvoiceGenerationParamsBuilder invoiceQRCodeGeneratorRequest(@Nullable InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest) {
            this.invoiceQRCodeGeneratorRequest = invoiceQRCodeGeneratorRequest;
            return this;
        }

        public InvoiceGenerationParamsBuilder templatePath(@Nullable String templatePath) {
            this.templatePath = templatePath;
            return this;
        }

        public InvoiceGenerationParamsBuilder customProperties(Map<String, Object> customProperties) {
            this.customProperties = customProperties;
            return this;
        }

        /**
         * @deprecated use {@link #languageLocale(String)} instead.
         */
        @Deprecated
        public InvoiceGenerationParamsBuilder language(Language language) {
            this.language = language;
            return this;
        }

        public InvoiceGenerationParamsBuilder languageLocale(@Nullable String languageLocale) {
            this.languageLocale = languageLocale;
            return this;
        }

        public InvoiceGenerationParamsBuilder templateRoot(Path templateRoot) {
            if (this.templateRoots == null) this.templateRoots = new ArrayList<>();
            this.templateRoots.add(templateRoot);
            return this;
        }

        public InvoiceGenerationParamsBuilder templateRoots(Collection<? extends Path> templateRoots) {
            Objects.requireNonNull(templateRoots, "template roots cannot be null");
            if (this.templateRoots == null) this.templateRoots = new ArrayList<>();
            this.templateRoots.addAll(templateRoots);
            return this;
        }

        public InvoiceGenerationParamsBuilder clearTemplateRoots() {
            if (this.templateRoots != null) this.templateRoots.clear();
            return this;
        }

        public InvoiceGenerationParams build() {
            return new InvoiceGenerationParams(this);
        }

        @Override
        public String toString() {
            return "InvoiceGenerationParams.InvoiceGenerationParamsBuilder(verificationLink=" + verificationLink
                    + ", logo=" + Arrays.toString(logo)
                    + ", logoUri=" + logoUri
                    + ", currencyDate=" + currencyDate
                    + ", issuerUser=" + issuerUser
                    + ", showCorrectionDifferences=" + showCorrectionDifferences
                    + ", schema=" + schema
                    + ", ksefNumber=" + ksefNumber
                    + ", invoiceQRCodeGeneratorRequest=" + invoiceQRCodeGeneratorRequest
                    + ", templatePath=" + templatePath
                    + ", customProperties=" + customProperties
                    + ", language=" + language
                    + ", languageLocale=" + languageLocale
                    + ", templateRoots=" + templateRoots + ")";
        }
    }
}
