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

/**
 * Parameters controlling how a KSeF invoice is rendered to PDF.
 */
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

    @Nullable
    private String templatePath;

    private Map<String, Object> customProperties;

    private Language language;

    @Nullable
    private String languageLocale;

    private final List<Path> templateRoots;

    /**
     * @deprecated use the builder instead.
     */
    @Deprecated
    public InvoiceGenerationParams() {
        this(builder().schema(InvoiceSchema.FA3_1_0_E));
    }

    /**
     * @param verificationLink the KSeF verification link
     * @param logo the raster logo image bytes
     * @param logoUri the logo image URI
     * @param currencyDate the exchange-rate date
     * @param issuerUser the issuing user label
     * @param showCorrectionDifferences whether to show correction differences
     * @param schema the invoice schema to render
     * @param ksefNumber the KSeF number, or {@code null} for an offline label
     * @param invoiceQRCodeGeneratorRequest the QR code generation request
     * @param templatePath the classpath-relative custom template path
     * @param customProperties the template-specific XSLT parameters
     * @param language the label language
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
        this.templateRoots = builder.templateRoots.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.templateRoots));
    }

    /**
     * Returns the KSeF verification link.
     * @return the verification link, or {@code null} if unset
     */
    @Nullable
    public String getVerificationLink() {
        return verificationLink;
    }

    /**
     * Sets the KSeF verification link.
     * @param verificationLink the verification link, or {@code null} to unset
     */
    public void setVerificationLink(@Nullable String verificationLink) {
        this.verificationLink = verificationLink;
    }

    /**
     * Returns the raster logo image bytes.
     * @return the logo bytes
     */
    public byte[] getLogo() {
        return logo;
    }

    /**
     * Sets the raster logo image bytes.
     * @param logo the logo bytes
     */
    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    /**
     * Returns the logo image URI.
     * @return the logo URI
     */
    public URI getLogoUri() {
        return logoUri;
    }

    /**
     * Sets the logo image URI.
     * @param logoUri the logo URI
     */
    public void setLogoUri(URI logoUri) {
        this.logoUri = logoUri;
    }

    /**
     * Returns the exchange-rate date.
     * @return the currency date, or {@code null} if unset
     */
    @Nullable
    public LocalDate getCurrencyDate() {
        return currencyDate;
    }

    /**
     * Sets the exchange-rate date.
     * @param currencyDate the currency date, or {@code null} to unset
     */
    public void setCurrencyDate(@Nullable LocalDate currencyDate) {
        this.currencyDate = currencyDate;
    }

    /**
     * Returns the issuing user label.
     * @return the issuer user, or {@code null} if unset
     */
    @Nullable
    public String getIssuerUser() {
        return issuerUser;
    }

    /**
     * Sets the issuing user label.
     * @param issuerUser the issuer user, or {@code null} to unset
     */
    public void setIssuerUser(@Nullable String issuerUser) {
        this.issuerUser = issuerUser;
    }

    /**
     * Tells whether correction differences are shown.
     * @return {@code true} if correction differences are shown
     */
    public boolean isShowCorrectionDifferences() {
        return showCorrectionDifferences;
    }

    /**
     * Sets whether correction differences are shown.
     * @param showCorrectionDifferences {@code true} to show correction differences
     */
    public void setShowCorrectionDifferences(boolean showCorrectionDifferences) {
        this.showCorrectionDifferences = showCorrectionDifferences;
    }

    /**
     * Returns the invoice schema to render.
     * @return the invoice schema
     */
    @NotNull
    public InvoiceSchema getSchema() {
        return schema;
    }

    /**
     * Sets the invoice schema to render.
     * @param schema the invoice schema, never {@code null}
     */
    public void setSchema(@NotNull InvoiceSchema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    /**
     * KSeF Number if provided, OFFLINE label will shown otherwise
     * @return the KSeF number, or {@code null} for an offline label
     */
    @Nullable
    public String getKsefNumber() {
        return ksefNumber;
    }

    /**
     * Sets the KSeF number.
     * @param ksefNumber the KSeF number, or {@code null} for an offline label
     */
    public void setKsefNumber(@Nullable String ksefNumber) {
        this.ksefNumber = ksefNumber;
    }

    /**
     * Returns the QR code generation request.
     * @return the QR code request, or {@code null} if unset
     */
    @Nullable
    public InvoiceQRCodeGeneratorRequest getInvoiceQRCodeGeneratorRequest() {
        return invoiceQRCodeGeneratorRequest;
    }

    /**
     * Sets the QR code generation request.
     * @param invoiceQRCodeGeneratorRequest the QR code request, or {@code null} to unset
     */
    public void setInvoiceQRCodeGeneratorRequest(@Nullable InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest) {
        this.invoiceQRCodeGeneratorRequest = invoiceQRCodeGeneratorRequest;
    }

    /**
     * Optional classpath-relative path to a custom XSLT invoice template.
     * <p>
     * Security note: This value must reference a trusted stylesheet available on the application's classpath.
     * The library does not validate where this path comes from; callers are responsible for ensuring that
     * untrusted users cannot control this value or the underlying XSLT content.
     * @return the custom template path, or {@code null} to use the schema default
     */
    @Nullable
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * Sets the classpath-relative path to a custom XSLT invoice template.
     * @param templatePath the custom template path, or {@code null} to use the schema default
     */
    public void setTemplatePath(@Nullable String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * Optional template-specific XSLT parameters forwarded to the transformer.
     * <p>
     * Security note: Values in this map are passed directly as XSLT parameters.
     * The library does not validate parameter names or values; callers are responsible for ensuring that
     * untrusted users cannot control this map when rendering trusted templates.
     * @return the custom XSLT parameters, or {@code null} if unset
     */
    @Nullable
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    /**
     * Sets the template-specific XSLT parameters forwarded to the transformer.
     * @param customProperties the custom XSLT parameters
     */
    public void setCustomProperties(Map<String, Object> customProperties) {
        this.customProperties = customProperties;
    }

    /**
     * @return the label language
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
     * @param language the label language
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
     *
     * @return the BCP&nbsp;47 language tag, or {@code null} if unset
     */
    @Nullable
    public String getLanguageLocale() {
        return languageLocale;
    }

    /**
     * Sets the BCP&nbsp;47 language tag used to select the label file.
     * @param languageLocale the BCP&nbsp;47 language tag, or {@code null} to unset
     */
    public void setLanguageLocale(@Nullable String languageLocale) {
        this.languageLocale = languageLocale;
    }

    /**
     * Returns an unmodifiable view of the configured filesystem template roots.
     * @return the template roots, never {@code null}
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
     *
     * @return the resolved BCP&nbsp;47 language tag, never {@code null}
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

    /**
     * Tells whether {@code other} may be compared for equality with this instance.
     * @param other the object to test
     * @return {@code true} if {@code other} is an {@code InvoiceGenerationParams}
     */
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

    /**
     * Creates a new builder for {@link InvoiceGenerationParams}.
     * @return a fresh builder
     */
    public static InvoiceGenerationParamsBuilder builder() {
        return new InvoiceGenerationParamsBuilder();
    }

    /**
     * Fluent builder for {@link InvoiceGenerationParams}.
     */
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
        private final ArrayList<Path> templateRoots = new ArrayList<>();

        InvoiceGenerationParamsBuilder() {
        }

        /**
         * Sets the KSeF verification link.
         * @param verificationLink the verification link, or {@code null} to unset
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder verificationLink(@Nullable String verificationLink) {
            this.verificationLink = verificationLink;
            return this;
        }

        /**
         * Sets the raster logo image bytes.
         * @param logo the logo bytes
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder logo(byte[] logo) {
            this.logo = logo;
            return this;
        }

        /**
         * Sets the logo image URI.
         * @param logoUri the logo URI
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder logoUri(URI logoUri) {
            this.logoUri = logoUri;
            return this;
        }

        /**
         * Sets the exchange-rate date.
         * @param currencyDate the currency date, or {@code null} to unset
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder currencyDate(@Nullable LocalDate currencyDate) {
            this.currencyDate = currencyDate;
            return this;
        }

        /**
         * Sets the issuing user label.
         * @param issuerUser the issuer user, or {@code null} to unset
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder issuerUser(@Nullable String issuerUser) {
            this.issuerUser = issuerUser;
            return this;
        }

        /**
         * Sets whether correction differences are shown.
         * @param showCorrectionDifferences {@code true} to show correction differences
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder showCorrectionDifferences(boolean showCorrectionDifferences) {
            this.showCorrectionDifferences = showCorrectionDifferences;
            return this;
        }

        /**
         * Sets the invoice schema to render.
         * @param schema the invoice schema
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder schema(@NotNull InvoiceSchema schema) {
            this.schema = schema;
            return this;
        }

        /**
         * Sets the KSeF number.
         * @param ksefNumber the KSeF number, or {@code null} for an offline label
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder ksefNumber(@Nullable String ksefNumber) {
            this.ksefNumber = ksefNumber;
            return this;
        }

        /**
         * Sets the QR code generation request.
         * @param invoiceQRCodeGeneratorRequest the QR code request, or {@code null} to unset
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder invoiceQRCodeGeneratorRequest(@Nullable InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest) {
            this.invoiceQRCodeGeneratorRequest = invoiceQRCodeGeneratorRequest;
            return this;
        }

        /**
         * Sets the classpath-relative path to a custom XSLT invoice template.
         * @param templatePath the custom template path, or {@code null} to use the schema default
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder templatePath(@Nullable String templatePath) {
            this.templatePath = templatePath;
            return this;
        }

        /**
         * Sets the template-specific XSLT parameters forwarded to the transformer.
         * @param customProperties the custom XSLT parameters
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder customProperties(Map<String, Object> customProperties) {
            this.customProperties = customProperties;
            return this;
        }

        /**
         * @param language the label language
         * @return this builder
         * @deprecated use {@link #languageLocale(String)} instead.
         */
        @Deprecated
        public InvoiceGenerationParamsBuilder language(Language language) {
            this.language = language;
            return this;
        }

        /**
         * Sets the BCP&nbsp;47 language tag used to select the label file.
         * @param languageLocale the BCP&nbsp;47 language tag, or {@code null} to unset
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder languageLocale(@Nullable String languageLocale) {
            this.languageLocale = languageLocale;
            return this;
        }

        /**
         * Appends a single filesystem template root searched before the classpath.
         * @param templateRoot the directory to append
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder templateRoot(Path templateRoot) {
            this.templateRoots.add(templateRoot);
            return this;
        }

        /**
         * Appends a collection of filesystem template roots searched before the classpath.
         * A {@code null} collection is ignored.
         * @param templateRoots the directories to append, or {@code null}
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder templateRoots(Collection<? extends Path> templateRoots) {
            if (templateRoots != null) {
                this.templateRoots.addAll(templateRoots);
            }
            return this;
        }

        /**
         * Removes all template roots accumulated so far.
         * @return this builder
         */
        public InvoiceGenerationParamsBuilder clearTemplateRoots() {
            this.templateRoots.clear();
            return this;
        }

        /**
         * Builds an immutable {@link InvoiceGenerationParams} from this builder's state.
         * @return the configured parameters
         */
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