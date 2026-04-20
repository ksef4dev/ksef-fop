package io.alapierre.ksef.fop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * Optional path to a custom XSLT invoice template.
     * <p>
     * <ul>
     *     <li>If this value is {@code null} or blank, the default built-in template is loaded from classpath based on schema.</li>
     *     <li>If this value is provided, lookup is performed in the following order:
     *         <ol>
     *             <li>each configured {@link #templateRoots} entry (in order)</li>
     *             <li>classpath fallback</li>
     *         </ol>
     *     </li>
     * </ul>
     * Security note: XSLT is executable content. Make sure untrusted users cannot control this value or the underlying XSLT file.
     */
    @Nullable
    private String templatePath;

    /**
     * Optional ordered list of filesystem roots used for resolving templates.
     * <p>
     * When configured, template lookup tries each root in order before classpath fallback.
     * This enables partial overrides where only selected templates are stored on disk and the rest
     * are inherited from built-in classpath templates.
     */
    @Builder.Default
    private List<Path> templateRoots = new ArrayList<>();

    /**
     * Optional template-specific XSLT parameters forwarded to the transformer.
     * <p>
     * Security note: Values in this map are passed directly as XSLT parameters.
     * The library does not validate parameter names or values; callers are responsible for ensuring that
     * untrusted users cannot control this map when rendering trusted templates.
     */
    @Builder.Default
    private Map<String, Object> customProperties = new HashMap<>();

    @Builder.Default
    private Language language = Language.PL;

    public InvoiceGenerationParams addTemplateRoot(@NotNull Path templateRoot) {
        if (templateRoots == null) {
            templateRoots = new ArrayList<>();
        }
        templateRoots.add(templateRoot);
        return this;
    }
}
