package io.alapierre.ksef.fop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

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
}
