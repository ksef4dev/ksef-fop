package io.alapierre.ksef.fop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
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

    @Builder.Default
    private Language language = Language.PL;
}
