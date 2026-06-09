package io.alapierre.ksef.fop;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    @Builder.Default
    private Map<String, Object> customProperties = new HashMap<>();

    /**
     * @deprecated use {@link #languageLocale} instead, which accepts any BCP&nbsp;47
     * language tag (e.g. {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}) and is not
     * limited to the values defined by this enum. Kept for backward compatibility.
     * When both are set, {@link #languageLocale} wins (see {@link #resolveLanguageTag()}).
     */
    @Deprecated
    @Builder.Default
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
     * Ordered list of resource roots searched before the classpath when resolving templates,
     * translations and relative logo paths.
     *
     * <p>Each entry is a {@link URI}: a {@code file:} directory (e.g.
     * {@code Paths.get("/etc/ksef").toUri()}) or an HTTP(S) base URL
     * (e.g. {@code URI.create("http://localhost:8077/xslt")}). Templates, {@code i18n/}
     * label files and logos are resolved relative to these roots.</p>
     */
    @Singular("resourceRoot")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<URI> resourceRoots;

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
        this.verificationLink = verificationLink;
        this.logo = logo;
        this.logoUri = logoUri;
        this.currencyDate = currencyDate;
        this.issuerUser = issuerUser;
        this.showCorrectionDifferences = showCorrectionDifferences;
        this.schema = schema;
        this.ksefNumber = ksefNumber;
        this.invoiceQRCodeGeneratorRequest = invoiceQRCodeGeneratorRequest;
        this.templatePath = templatePath;
        this.customProperties = customProperties != null ? customProperties : new HashMap<>();
        this.language = language != null ? language : Language.PL;
        this.languageLocale = null;
        this.resourceRoots = Collections.emptyList();
    }

    /**
     * Returns an unmodifiable view of the configured resource roots.
     */
    public List<URI> getResourceRoots() {
        if (resourceRoots == null) return Collections.emptyList();
        return Collections.unmodifiableList(resourceRoots);
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
}
