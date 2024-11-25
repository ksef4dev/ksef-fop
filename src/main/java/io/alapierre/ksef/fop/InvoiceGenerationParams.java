package io.alapierre.ksef.fop;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceGenerationParams {
    @Nullable
    private String ksefNumber;
    @Nullable private String verificationLink;
    private byte[] qrCode;
    private byte[] logo;
    @Nullable
    private LocalDate currencyDate;
    @Nullable private String issuerUser;

    /**
     * @deprecated This constructor is deprecated because the parameters in this class may change in future versions, making it difficult to maintain compatibility.
     * It is recommended to use the builder pattern instead, which provides flexibility and helps accommodate future changes more easily.
     * Use {@link InvoiceGenerationParamsBuilder} to construct instances of this class.
     */
    @Deprecated
    public InvoiceGenerationParams(@Nullable String ksefNumber, @Nullable String verificationLink, byte[] qrCode, byte[] logo, @Nullable LocalDate currencyDate) {
        this.ksefNumber = ksefNumber;
        this.verificationLink = verificationLink;
        this.qrCode = qrCode;
        this.logo = logo;
        this.currencyDate = currencyDate;
    }
}
